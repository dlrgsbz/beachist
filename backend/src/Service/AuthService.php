<?php
declare(strict_types=1);


namespace App\Service;


use App\Interfaces\UserReader;

class AuthService {
    private UserReader $userReader;

    public function __construct(UserReader $userReader) {
        $this->userReader = $userReader;
    }

    public function listUsers() {
        return $this->userReader->listUsers();
    }
}
