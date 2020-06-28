<?php
declare(strict_types=1);

namespace App\Service;

use App\Entity\Station;
use App\Entity\StationField;
use App\Interfaces\StationFieldReader;
use App\Interfaces\StationReader;
use App\Interfaces\StationWriter;

class StationService {
    private StationReader $stationReader;
    private StationWriter $stationWriter;
    private StationFieldReader $stationFieldReader;

    public function __construct(
        StationReader $stationReader,
        StationWriter $stationWriter,
        StationFieldReader $stationFieldReader
    ) {
        $this->stationReader = $stationReader;
        $this->stationWriter = $stationWriter;
        $this->stationFieldReader = $stationFieldReader;
    }

    /**
     * @return Station[]
     */
    public function getStations(): array {
        return $this->stationReader->getStations();
    }

    public function getStation(string $id): ?Station {
        return $this->stationReader->getStation($id);
    }

    /**
     * @return StationField[]
     */
    public function getFields(string $id): array {
        return $this->stationFieldReader->getForStation($id);
    }

    public function getField(string $stationId, string $fieldId): ?StationField {
        return $this->stationFieldReader->get($stationId, $fieldId);
    }
}
