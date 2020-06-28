<?php
declare(strict_types=1);


namespace App\Interfaces;


use App\Entity\DailyStats;

interface EventReader {
    function get(\DateTimeInterface $date): DailyStats;

    function getByStation(\DateTimeInterface $date, string $stationId);
}
