<?php
declare(strict_types=1);

namespace App\Interfaces;


use App\Entity\Station;

interface StationReader {
    /**
     * @return Station[]
     */
    function getStations(): array;

    function getStation(string $id): ?Station;
}

class StationNotFoundException extends \Exception {
}
