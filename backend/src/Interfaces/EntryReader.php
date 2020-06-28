<?php
declare(strict_types=1);


namespace App\Interfaces;


use DateTimeInterface;

interface EntryReader {
    function get(DateTimeInterface $date): array;

    function getByStation(DateTimeInterface $date, string $stationId): array;
}
