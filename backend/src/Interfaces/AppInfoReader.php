<?php
declare(strict_types=1);


namespace App\Interfaces;


use App\Entity\AppInfo;

interface AppInfoReader {
    function getLatestAppInfo(string $id): ?AppInfo;
}
