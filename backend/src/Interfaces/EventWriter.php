<?php
declare(strict_types=1);


namespace App\Interfaces;


use App\Entity\Event;
use Doctrine\DBAL\Exception\UniqueConstraintViolationException;

interface EventWriter {
    /** @throws UniqueConstraintViolationException */
    function create(Event $event): string ;
}
