<?php
declare(strict_types=1);

namespace App\Interfaces;


use App\Entity\Field;

interface FieldReader {
    /**
     * @return Field[]
     */
    function getAll(): array;

    function get(string $id): ?Field;
}

class FieldNotFoundException extends \Exception{}
