<?php
declare(strict_types=1);


namespace App\Tests\Integration;


use App\Entity\Station;
use Ramsey\Uuid\Uuid;
use Doctrine\Persistence\ObjectManager;
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

    private function setupStation() {
        $station = new Station(Uuid::uuid4()->toString(), 'Test-Station');
        $this->entityManager->persist($station);
        $this->entityManager->flush();

        $this->stationId = $station->id;
    }

    public function testCreateEvent() {
        $parameters = [
            'type' => 'firstAid',
        ];

        $stationId = $this->stationId;
        $uri = "/api/station/${stationId}/event";
        $this->client->request('POST', $uri, $parameters);

        $this->assertEquals(201, $this->client->getResponse()->getStatusCode());
    }
}
