<?php
declare(strict_types=1);


namespace App\Interfaces;


use App\Entity\Station;

interface VersionWriter {
    function setAppVersion(Station $station, string $version);
}
