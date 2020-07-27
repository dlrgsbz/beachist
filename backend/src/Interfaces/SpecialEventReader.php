<?php
declare(strict_types=1);


namespace App\Interfaces;


use App\Entity\SpecialEvent;
use DateTimeInterface;

interface SpecialEventReader {

    /**
     * @return SpecialEvent[]
     */
    function get(DateTimeInterface $date): array;

    /**
     * @return SpecialEvent[]
     */
    function getByStation(DateTimeInterface $date, string $stationId): array;
}
