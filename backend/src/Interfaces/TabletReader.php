<?php
declare(strict_types=1);


namespace App\Interfaces;


use App\Entity\Tablet;

interface TabletReader {
    /**
     * @return Tablet[]
     */
    function getTablets(): array;

    function getTablet(string $id): ?Tablet;
}
