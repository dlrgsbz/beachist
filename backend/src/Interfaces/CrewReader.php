<?php
declare(strict_types=1);

namespace App\Interfaces;

use App\Entity\CrewInfo;

interface CrewReader {
    /**
     * @return CrewInfo[]
     */
    function getCrews(\DateTime $date): array;
}
