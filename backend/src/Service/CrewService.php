<?php
declare(strict_types=1);

namespace App\Service;

use App\Entity\CrewInfo;
use App\Interfaces\CrewReader;
use App\Interfaces\CrewWriter;
use App\Interfaces\StationNotFoundException;
use App\Interfaces\StationReader;

class CrewService
{
    private StationReader $stationReader;
    private CrewReader $crewReader;
    private CrewWriter $crewWriter;

    public function __construct(StationReader $stationReader, CrewReader $crewReader, CrewWriter $crewWriter) {
        $this->stationReader = $stationReader;
        $this->crewReader = $crewReader;
        $this->crewWriter = $crewWriter;
    }

    /**
     * @throws StationNotFoundException
     */
    function addCrew(string $stationId, string $crew, \DateTimeInterface $date): void {
        $station = $this->stationReader->getStation($stationId);
        if (!$station) {
            throw new StationNotFoundException();
        }

        $crewInfo = new CrewInfo($station, $crew, $date);

        $this->crewWriter->upsert($crewInfo);
    }

    public function getCrews(\DateTimeInterface $date) {
        return $this->crewReader->getCrews($date);
    }
}