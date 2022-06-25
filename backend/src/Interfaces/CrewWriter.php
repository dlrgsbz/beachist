<?php
declare(strict_types=1);

namespace App\Interfaces;

use App\Entity\CrewInfo;

interface CrewWriter {
    function upsert(CrewInfo $crewInfo): void;
}
