<?php
declare(strict_types=1);


namespace App\Interfaces;


use App\Entity\Tablet;

interface TabletWriter {
    function createTablet(Tablet $tablet): string;
}
