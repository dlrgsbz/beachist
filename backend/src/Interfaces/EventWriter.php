<?php
declare(strict_types=1);


namespace App\Interfaces;


use App\Entity\Event;

interface EventWriter {
    function create(Event $event): int;
}
