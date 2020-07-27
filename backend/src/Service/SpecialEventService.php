<?php
declare(strict_types=1);


namespace App\Service;


use App\Entity\SpecialEvent;
use App\Enum\SpecialEventType;
use App\Interfaces\SpecialEventReader;
use App\Interfaces\SpecialEventWriter;
use App\Interfaces\StationNotFoundException;
use App\Interfaces\StationReader;
use DateTime;
use DateTimeInterface;
use Ramsey\Uuid\UuidInterface;

class SpecialEventService {
    private SpecialEventWriter $specialEventWriter;
    private SpecialEventReader $specialEventReader;
    private StationReader $stationReader;

    public function __construct(SpecialEventWriter $specialEventWriter, SpecialEventReader $specialEventReader, StationReader $stationReader) {
        $this->specialEventWriter = $specialEventWriter;
        $this->specialEventReader = $specialEventReader;
        $this->stationReader = $stationReader;
    }

    /** @throws */
    public function create(string $stationId, string $title, string $note, string $notifier, SpecialEventType $type, DateTime $date = null): UuidInterface {
        $station = $this->stationReader->getStation($stationId);
        if (!$station) {
            throw new StationNotFoundException();
        }

        $event = new SpecialEvent($station, $title, $note, $notifier, $type, $date);

        return $this->specialEventWriter->create($event);
    }

    public function get(DateTimeInterface $date): array {
        return $this->specialEventReader->get($date);
    }

    /** @throws */
    public function getByStation(DateTimeInterface $date, string $stationId): array {
        $station = $this->stationReader->getStation($stationId);
        if (!$station) {
            throw new StationNotFoundException();
        }

        return $this->specialEventReader->getByStation($date, $stationId);
    }
}
