<?php
declare(strict_types=1);

namespace App\Service;

use App\Entity\AppInfo;
use App\Entity\Station;
use App\Entity\StationField;
use App\Entity\StationProvisioningRequest;
use App\Interfaces\StationFieldReader;
use App\Interfaces\StationFieldWriter;
use App\Interfaces\StationReader;
use App\Interfaces\StationWriter;
use App\Interfaces\AppInfoReader;
use App\Interfaces\AppInfoWriter;
use App\Interfaces\FieldNotFoundException;
use App\Interfaces\FieldReader;
use App\Interfaces\StationNotFoundException;
use App\Repository\ProvisioningRepository;
use DateTime;

class StationService {
    private StationReader $stationReader;
    private StationWriter $stationWriter;
    private StationFieldReader $stationFieldReader;
    private StationFieldWriter $stationFieldWriter;
    private FieldReader $fieldReader;
    private AppInfoReader $appInfoReader;
    private AppInfoWriter $appInfoWriter;
    private ProvisioningRepository $provisioningRepository;

    public function __construct(
        StationReader          $stationReader,
        StationWriter          $stationWriter,
        StationFieldReader     $stationFieldReader,
        StationFieldWriter     $stationFieldWriter,
        FieldReader            $fieldReader,
        AppInfoReader          $versionReader,
        AppInfoWriter          $versionWriter,
        ProvisioningRepository $provisioningRepository
    ) {
        $this->stationReader = $stationReader;
        $this->stationWriter = $stationWriter;
        $this->stationFieldReader = $stationFieldReader;
        $this->stationFieldWriter = $stationFieldWriter;
        $this->fieldReader = $fieldReader;
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
     * @return StationField[]
     */
    public function getAllAssignments(): array {
        return $this->stationFieldReader->getAll();
    }

    /**
     * Assigns a field to a station. A null $stationId assigns the field to all
     * stations (global assignment).
     *
     * @throws StationNotFoundException
     * @throws FieldNotFoundException
     */
    public function assignField(string $fieldId, ?string $stationId): StationField {
        $this->assertFieldExists($fieldId);
        $this->assertStationExists($stationId);

        return $this->stationFieldWriter->assign($fieldId, $stationId);
    }

    /**
     * @throws StationNotFoundException
     * @throws FieldNotFoundException
     */
    public function unassignField(string $fieldId, ?string $stationId): void {
        $this->assertFieldExists($fieldId);
        $this->assertStationExists($stationId);

        $this->stationFieldWriter->unassign($fieldId, $stationId);
    }

    /**
     * Replaces all assignments of a field. When $global is true the field is
     * assigned to all stations (single global row); otherwise it is assigned to
     * exactly the given stations. The optional required amount and note are
     * applied to every created assignment row.
     *
     * @param string[] $stationIds
     *
     * @return StationField[]
     * @throws StationNotFoundException
     * @throws FieldNotFoundException
     */
    public function setFieldAssignments(string $fieldId, bool $global, array $stationIds, ?int $required = null, ?string $note = null): array {
        $this->assertFieldExists($fieldId);
        foreach ($stationIds as $stationId) {
            $this->assertStationExists($stationId);
        }

        $this->stationFieldWriter->unassignAllForField($fieldId);

        if ($global) {
            $this->stationFieldWriter->assign($fieldId, null, $required, $note);
        } else {
            foreach (array_unique($stationIds) as $stationId) {
                $this->stationFieldWriter->assign($fieldId, $stationId, $required, $note);
            }
        }

        return $this->stationFieldReader->getForField($fieldId);
    }

    /**
     * @throws FieldNotFoundException
     */
    private function assertFieldExists(string $fieldId): void {
        if (!$this->fieldReader->get($fieldId)) {
            throw new FieldNotFoundException();
        }
    }

    /**
     * @throws StationNotFoundException
     */
    private function assertStationExists(?string $stationId): void {
        if ($stationId === null) {
            return;
        }
        if (!$this->stationReader->getStation($stationId)) {
            throw new StationNotFoundException();
        }
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

        $provisionMap = [];
        /** @var StationProvisioningRequest $provision */
        foreach ($provisions as $provision) {
            $provisionMap[$provision->station->id] = $provision;
        }

        return $provisionMap;
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
