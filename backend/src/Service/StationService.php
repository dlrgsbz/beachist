<?php
declare(strict_types=1);

namespace App\Service;

use App\Entity\Station;
use App\Entity\StationField;
use App\Entity\StationProvisioningRequest;
use App\Interfaces\StationFieldReader;
use App\Interfaces\StationReader;
use App\Interfaces\StationWriter;
use App\Interfaces\VersionReader;
use App\Interfaces\VersionWriter;
use App\Interfaces\StationNotFoundException;
use App\Repository\ProvisioningRepository;
use DateTime;

class StationService {
    private StationReader $stationReader;
    private StationWriter $stationWriter;
    private StationFieldReader $stationFieldReader;
    private VersionReader $versionReader;
    private VersionWriter $versionWriter;
    private ProvisioningRepository $provisioningRepository;

    public function __construct(
        StationReader $stationReader,
        StationWriter $stationWriter,
        StationFieldReader $stationFieldReader,
        VersionReader $versionReader,
        VersionWriter $versionWriter,
        ProvisioningRepository $provisioningRepository
    ) {
        $this->stationReader = $stationReader;
        $this->stationWriter = $stationWriter;
        $this->stationFieldReader = $stationFieldReader;
        $this->versionReader = $versionReader;
        $this->versionWriter = $versionWriter;
        $this->provisioningRepository = $provisioningRepository;
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

    public function createProvisioning(string $stationId): StationProvisioningRequest {
        $station = $this->stationReader->getStation($stationId);
        if (!$station) {
            throw new StationNotFoundException();
        }

        $password = generatePassword();

        return $this->provisioningRepository->createProvisioning($station, $password);
    }

    public function provisionDevice(string $password): string {
        $request = $this->provisioningRepository->getProvisioning($password);

        if (!$request) {
            throw new StationNotFoundException();
        }

        if ($request->expiresAt <= new DateTime()) {
            throw new StationNotFoundException();
        }

        $stationId = $request->station->id;

        // todo: set request active after it has been used

        return $stationId;
    }

    public function listProvisions(): array {
        $provisions = $this->provisioningRepository->getUnexpiredProvisions();

        return $provisions;
    }
}

function generatePassword(int $length = 12): string {
    $keyspace = '0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ_-/';
    $str = '';
    $max = mb_strlen($keyspace, '8bit') - 1;
    if ($max < 1) {
        throw new \Exception('$keyspace must be at least two characters long');
    }
    for ($i = 0; $i < $length; ++$i) {
        $str .= $keyspace[random_int(0, $max)];
    }
    return $str;
}
