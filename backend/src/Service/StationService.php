<?php
declare(strict_types=1);

namespace App\Service;

use App\Entity\AppInfo;
use App\Entity\Station;
use App\Entity\StationField;
use App\Entity\StationProvisioningRequest;
use App\Interfaces\StationFieldReader;
use App\Interfaces\StationReader;
use App\Interfaces\StationWriter;
use App\Interfaces\AppInfoReader;
use App\Interfaces\AppInfoWriter;
use App\Interfaces\StationNotFoundException;
use App\Repository\ProvisioningRepository;
use DateTime;

class StationService {
    private StationReader $stationReader;
    private StationWriter $stationWriter;
    private StationFieldReader $stationFieldReader;
    private AppInfoReader $appInfoReader;
    private AppInfoWriter $appInfoWriter;
    private ProvisioningRepository $provisioningRepository;

    public function __construct(
        StationReader          $stationReader,
        StationWriter          $stationWriter,
        StationFieldReader     $stationFieldReader,
        AppInfoReader          $versionReader,
        AppInfoWriter          $versionWriter,
        ProvisioningRepository $provisioningRepository
    ) {
        $this->stationReader = $stationReader;
        $this->stationWriter = $stationWriter;
        $this->stationFieldReader = $stationFieldReader;
        $this->appInfoReader = $versionReader;
        $this->appInfoWriter = $versionWriter;
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

    /**
     * @throws StationNotFoundException
     */
    public function updateAppInfo(string $stationId, string $appVersion, int $appVersionCode, bool $connected) {
        $station = $this->stationReader->getStation($stationId);
        if (!$station) {
            throw new StationNotFoundException();
        }

        $this->appInfoWriter->setAppInfo($station, $appVersion, $appVersionCode, $connected);
    }

    public function getAppInfo(string $stationId): AppInfo {
        return $this->appInfoReader->getLatestAppInfo($stationId);
    }

    public function getLatestInfoMap(): array {
        $stations = $this->getStations();
        return array_reduce($stations, function (array $carry, Station $station) {
            $info = $this->appInfoReader->getLatestAppInfo($station->id);

            $carry[$station->id] = $info;
            return $carry;
        }, []);
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
