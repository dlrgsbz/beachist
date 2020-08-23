<?php
declare(strict_types=1);


namespace App\Interfaces;


interface VersionReader {
    function getLatestAppVersion(string $id): ?string;
}
