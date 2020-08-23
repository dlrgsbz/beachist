<?php
declare(strict_types=1);

namespace App\Service;

use App\Entity\Station;
use App\Entity\StationField;
use App\Interfaces\StationFieldReader;
use App\Interfaces\StationReader;
use App\Interfaces\StationWriter;
use App\Interfaces\VersionReader;
use App\Interfaces\VersionWriter;

class StationService {
    private StationReader $stationReader;
    private StationWriter $stationWriter;
    private StationFieldReader $stationFieldReader;
    private VersionReader $versionReader;
    private VersionWriter $versionWriter;

    public function __construct(
        StationReader $stationReader,
        StationWriter $stationWriter,
        StationFieldReader $stationFieldReader,
        VersionReader $versionReader,
        VersionWriter $versionWriter
    ) {
        $this->stationReader = $stationReader;
        $this->stationWriter = $stationWriter;
        $this->stationFieldReader = $stationFieldReader;
        $this->versionReader = $versionReader;
        $this->versionWriter = $versionWriter;
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

    public function setAppVersion(string $stationId, string $appVersion): void {
        $station = $this->stationReader->getStation($stationId);
        if (!$station) {
            return;
        }

        $storedVersion = $this->versionReader->getLatestAppVersion($stationId);

        if (!$storedVersion || !version_compare($appVersion, $storedVersion, 'eq')) {
            $this->versionWriter->setAppVersion($station, $appVersion);
        }
    }
}
