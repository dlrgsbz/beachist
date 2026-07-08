<?php
declare(strict_types=1);

namespace App\Interfaces;


use App\Entity\StationField;

interface StationFieldReader {
    /**
     * @return StationField[]
     */
    function getAll(): array;

    /**
     * @return StationField[]
     */
    function getForStation(string $stationId): array;

    function get(string $stationId, string $fieldId): ?StationField;

    /**
     * Returns every assignment row (station-specific and global) of a field.
     *
     * @return StationField[]
     */
    function getForField(string $fieldId): array;

    /**
     * Finds a single assignment row for a field and a station. A null
     * $stationId looks up the global assignment row.
     */
    function findAssignment(string $fieldId, ?string $stationId): ?StationField;
}

class StationFieldNotFoundException extends \Exception {
}
