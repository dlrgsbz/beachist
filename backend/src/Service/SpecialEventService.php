<?php
declare(strict_types=1);


namespace App\Service;


use App\Entity\SpecialEvent;
use App\Interfaces\SpecialEventReader;
use App\Interfaces\SpecialEventWriter;
use App\Interfaces\StationNotFoundException;
use App\Interfaces\StationReader;
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
    public function create(string $stationId, string $note, \DateTime $date = null): UuidInterface {
        $station = $this->stationReader->getStation($stationId);
        if (!$station) {
            throw new StationNotFoundException();
        }

        $event = new SpecialEvent($station, $note, $date);

        return $this->specialEventWriter->create($event);
    }
}
