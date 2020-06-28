<?php
declare(strict_types=1);


namespace App\Interfaces;


use App\Entity\Entry;

interface EntryWriter {
    function create(Entry $entry): string ;
}
