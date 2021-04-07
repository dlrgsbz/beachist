<?php
declare(strict_types=1);


namespace App\Tests\Integration;


use App\Entity\Station;
use Doctrine\Persistence\ObjectManager;
use Ramsey\Uuid\Uuid;
use Symfony\Bundle\FrameworkBundle\KernelBrowser;
use Symfony\Bundle\FrameworkBundle\Test\WebTestCase;

class StationEventTest extends WebTestCase {
    private KernelBrowser $client;
    private ObjectManager $entityManager;
    private string $stationId;

    protected function setUp(): void {
        $this->client = static::createClient();
        $container = $this->client->getContainer();
        $doctrine = $container->get('doctrine');
        $this->entityManager = $doctrine->getManager();

        $this->setupStation();

        parent::setUp();
    }

    private function request(string $method, string $uri, array $body = null) {
        if ($body) {
            $this->client->request($method, $uri, [], [], ['CONTENT_TYPE' => 'application/json'], json_encode($body));
        } else {
            $this->client->request($method, $uri);
        }
    }

    private function setupStation() {
        $station = new Station(Uuid::uuid4()->toString(), 'Test-Station');
        $this->entityManager->persist($station);
        $this->entityManager->flush();

        $this->stationId = $station->id;
    }

    public function testCreateEvent() {
        $data = [
            'type' => 'firstAid',
        ];

        $stationId = $this->stationId;
        $uri = "/api/station/${stationId}/event";
        $this->request('POST', $uri, $data);

        $this->assertEquals(201, $this->client->getResponse()->getStatusCode());
    }

    public function testCreateEventWithDateAndId() {
        $uuid = Uuid::uuid4()->toString();
        $data = [
            'type' => 'firstAid',
            'id' => $uuid,
            'date' => (new \DateTime())->format(\DateTime::ATOM),
        ];

        $stationId = $this->stationId;
        $uri = "/api/station/${stationId}/event";
        $this->request('POST', $uri, $data);

        $this->assertEquals(201, $this->client->getResponse()->getStatusCode());

        $response_data = json_decode($this->client->getResponse()->getContent(), true);
        $this->assertEquals($response_data['id'], $uuid);
    }

    public function testCreateEventWithDateAndIdIdempotency() {
        $uuid = Uuid::uuid4()->toString();
        $data = [
            'type' => 'firstAid',
            'id' => $uuid,
            'date' => (new \DateTime())->format(\DateTime::ATOM),
        ];

        $stationId = $this->stationId;
        $uri = "/api/station/${stationId}/event";
        $this->request('POST', $uri, $data);

        $this->assertEquals(201, $this->client->getResponse()->getStatusCode());

        $this->request('POST', $uri, $data);

        $this->assertEquals(201, $this->client->getResponse()->getStatusCode());

        $response_data = json_decode($this->client->getResponse()->getContent(), true);
        $this->assertEquals($response_data['id'], $uuid);
    }
}
