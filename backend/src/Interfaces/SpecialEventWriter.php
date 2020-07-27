<?php
declare(strict_types=1);


namespace App\Interfaces;


use App\Entity\SpecialEvent;
use Ramsey\Uuid\UuidInterface;

interface SpecialEventWriter {
    function create(SpecialEvent $event): UuidInterface;
}
