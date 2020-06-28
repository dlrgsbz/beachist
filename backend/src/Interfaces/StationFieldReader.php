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
}

class StationFieldNotFoundException extends \Exception {
}
