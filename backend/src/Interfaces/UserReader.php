<?php
declare(strict_types=1);


namespace App\Interfaces;


use App\Entity\User;

interface UserReader {
    public function listUsers(): array;
    public function getById(int $id): ?User;
}
