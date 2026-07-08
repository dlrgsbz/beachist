<?php
declare(strict_types=1);


namespace App\Tests\Integration;


use App\Entity\Field;
use App\Entity\Station;
use App\Entity\StationField;
use App\Entity\User;
use Doctrine\Persistence\ObjectManager;
use Lcobucci\JWT\Configuration;
use Lcobucci\JWT\Signer\Hmac\Sha256;
use Lcobucci\JWT\Signer\Key\InMemory;
use Ramsey\Uuid\Uuid;
use Symfony\Bundle\FrameworkBundle\KernelBrowser;
use Symfony\Bundle\FrameworkBundle\Test\WebTestCase;

class StationFieldTest extends WebTestCase {
    private KernelBrowser $client;
    private ObjectManager $entityManager;
    private string $stationId;
    private string $token;

    protected function setUp(): void {
        $this->client = static::createClient();
        $container = $this->client->getContainer();
        $doctrine = $container->get('doctrine');
        $this->entityManager = $doctrine->getManager();

        $this->setupStation();
        $this->loginAsAdmin();

        parent::setUp();
    }

    private function request(string $method, string $uri, array $body = null) {
        $headers = ['HTTP_AUTHORIZATION' => 'Bearer ' . $this->token];
        if ($body !== null) {
            $headers['CONTENT_TYPE'] = 'application/json';
            $this->client->request($method, $uri, [], [], $headers, json_encode($body));
        } else {
            $this->client->request($method, $uri, [], [], $headers);
        }
    }

    private function setupStation() {
        $station = new Station(Uuid::uuid4()->toString(), 'Test-Station');
        $this->entityManager->persist($station);
        $this->entityManager->flush();

        $this->stationId = $station->id;
    }

    private function loginAsAdmin() {
        $user = new User();
        $user->name = 'admin-' . Uuid::uuid4()->toString();
        $user->roles = ['ROLE_ADMIN'];
        $user->password = 'irrelevant';
        $user->description = 'test admin';
        $this->entityManager->persist($user);
        $this->entityManager->flush();

        $this->token = $this->createToken((string) $user->id);
    }

    private function createToken(string $userId): string {
        $key = InMemory::base64Encoded($_ENV['APP_JWT_SECRET']);
        $configuration = Configuration::forSymmetricSigner(new Sha256(), $key);

        $now = new \DateTimeImmutable();
        $token = $configuration->builder()
            ->relatedTo($userId)
            ->issuedAt($now)
            ->expiresAt($now->modify('+1 day'))
            ->canOnlyBeUsedAfter($now->modify('-1 second'))
            ->getToken($configuration->signer(), $configuration->signingKey());

        return $token->toString();
    }

    public function testCreateField(): string {
        $this->request('POST', '/api/field', ['name' => 'Rettungsboje', 'sortId' => 5]);

        $this->assertEquals(201, $this->client->getResponse()->getStatusCode());

        $data = json_decode($this->client->getResponse()->getContent(), true);
        $this->assertEquals('Rettungsboje', $data['name']);
        $this->assertEquals(5, $data['sortId']);
        $this->assertFalse($data['deleted']);

        return $data['id'];
    }

    public function testCreateFieldRequiresName() {
        $this->request('POST', '/api/field', ['sortId' => 1]);

        $this->assertEquals(400, $this->client->getResponse()->getStatusCode());
    }

    public function testUpdateField() {
        $id = $this->createField('Original');

        $this->request('PUT', "/api/field/${id}", ['name' => 'Geändert', 'sortId' => 9]);

        $this->assertEquals(200, $this->client->getResponse()->getStatusCode());
        $data = json_decode($this->client->getResponse()->getContent(), true);
        $this->assertEquals('Geändert', $data['name']);
        $this->assertEquals(9, $data['sortId']);
    }

    public function testSoftDeleteFieldRemovesItFromListingsAndAssignments() {
        $id = $this->createField('Zu löschen');
        $this->assign($id, $this->stationId);

        $this->request('DELETE', "/api/field/${id}");
        $this->assertEquals(204, $this->client->getResponse()->getStatusCode());

        // no longer listed
        $this->request('GET', '/api/field');
        $fields = json_decode($this->client->getResponse()->getContent(), true);
        $ids = array_column($fields, 'id');
        $this->assertNotContains($id, $ids);

        // no longer assigned to the station
        $this->request('GET', "/api/station/{$this->stationId}/field");
        $stationFields = json_decode($this->client->getResponse()->getContent(), true);
        $this->assertNotContains($id, array_column($stationFields, 'id'));
    }

    public function testAssignAndUnassignField() {
        $id = $this->createField('Zuweisbar');

        $this->assign($id, $this->stationId);
        $this->assertEquals(201, $this->client->getResponse()->getStatusCode());

        $this->request('GET', "/api/station/{$this->stationId}/field");
        $stationFields = json_decode($this->client->getResponse()->getContent(), true);
        $this->assertContains($id, array_column($stationFields, 'id'));

        $this->request('DELETE', "/api/station/{$this->stationId}/field/${id}");
        $this->assertEquals(204, $this->client->getResponse()->getStatusCode());

        $this->request('GET', "/api/station/{$this->stationId}/field");
        $stationFields = json_decode($this->client->getResponse()->getContent(), true);
        $this->assertNotContains($id, array_column($stationFields, 'id'));
    }

    public function testAssignGlobalSupersedesStationAssignment() {
        $id = $this->createField('Global');
        $this->assign($id, $this->stationId);

        $this->request('POST', "/api/field/${id}/global");
        $this->assertEquals(201, $this->client->getResponse()->getStatusCode());

        // exactly one assignment row, and it is the global (null station) one
        $assignments = $this->entityManager->getRepository(StationField::class)->findBy(['field' => $id]);
        $this->assertCount(1, $assignments);
        $this->assertNull($assignments[0]->station);
    }

    public function testReorderFieldsRenumbersSortId() {
        $a = $this->createField('Alpha');
        $b = $this->createField('Beta');
        $c = $this->createField('Gamma');

        $this->request('PUT', '/api/field/reorder', ['ids' => [$c, $a, $b]]);
        $this->assertEquals(200, $this->client->getResponse()->getStatusCode());

        $this->request('GET', '/api/field');
        $fields = json_decode($this->client->getResponse()->getContent(), true);
        $order = array_values(array_filter(array_column($fields, 'id'), fn ($id) => in_array($id, [$a, $b, $c], true)));

        $this->assertEquals([$c, $a, $b], $order);
    }

    public function testReorderUnknownFieldReturns404() {
        $a = $this->createField('Alpha');

        $this->request('PUT', '/api/field/reorder', ['ids' => [$a, Uuid::uuid4()->toString()]]);

        $this->assertEquals(404, $this->client->getResponse()->getStatusCode());
    }

    public function testSetAssignmentsReplacesStations() {
        $id = $this->createField('Bulk');
        $secondStation = new Station(Uuid::uuid4()->toString(), 'Zweite Station');
        $this->entityManager->persist($secondStation);
        $this->entityManager->flush();

        $this->request('PUT', "/api/field/${id}/assignments", [
            'global' => false,
            'stations' => [$this->stationId, $secondStation->id],
            'required' => 3,
            'note' => 'Bitte vollständig prüfen',
        ]);
        $this->assertEquals(200, $this->client->getResponse()->getStatusCode());

        $assignments = $this->entityManager->getRepository(StationField::class)->findBy(['field' => $id]);
        $stationIds = array_map(fn (StationField $sf) => $sf->station ? $sf->station->id : null, $assignments);
        sort($stationIds);
        $expected = [$this->stationId, $secondStation->id];
        sort($expected);
        $this->assertEquals($expected, $stationIds);
        foreach ($assignments as $assignment) {
            $this->assertSame(3, $assignment->required);
            $this->assertSame('Bitte vollständig prüfen', $assignment->note);
        }

        // switching to global collapses to a single global row and clears the amount/note
        $this->request('PUT', "/api/field/${id}/assignments", ['global' => true, 'stations' => []]);
        $this->assertEquals(200, $this->client->getResponse()->getStatusCode());
        $this->entityManager->clear();
        $assignments = $this->entityManager->getRepository(StationField::class)->findBy(['field' => $id]);
        $this->assertCount(1, $assignments);
        $this->assertNull($assignments[0]->station);
        $this->assertNull($assignments[0]->required);
        $this->assertNull($assignments[0]->note);

        // empty, non-global clears all rows
        $this->request('PUT', "/api/field/${id}/assignments", ['global' => false, 'stations' => []]);
        $this->assertEquals(200, $this->client->getResponse()->getStatusCode());
        $this->entityManager->clear();
        $assignments = $this->entityManager->getRepository(StationField::class)->findBy(['field' => $id]);
        $this->assertCount(0, $assignments);
    }

    private function createField(string $name): string {
        $this->request('POST', '/api/field', ['name' => $name]);
        $data = json_decode($this->client->getResponse()->getContent(), true);
        return $data['id'];
    }

    private function assign(string $fieldId, string $stationId): void {
        $this->request('POST', "/api/station/${stationId}/field/${fieldId}");
    }
}
