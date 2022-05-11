<?php
declare(strict_types=1);


namespace App\Interfaces;


use App\Entity\Station;

interface AppInfoWriter {
    function setAppInfo(Station $station, string $version, int $versionCode, bool $connected);
}
