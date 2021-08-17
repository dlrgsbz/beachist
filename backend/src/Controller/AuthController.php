<?php
declare(strict_types=1);


namespace App\Controller;

use App\Entity\User;
use App\Service\AuthService;
use App\Service\JwtService;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Annotation\Route;

/**
 * @Route("/auth", name="auth_")
 */
class AuthController extends AbstractController {
    private AuthService $authService;
    private JwtService $jwtService;

    public function __construct(AuthService $authService, JwtService $jwtService) {
        $this->authService = $authService;
        $this->jwtService = $jwtService;
    }

    /**
     * @Route("/users", name="user_list")
     */
    public function listUsersAction(): Response {
        $users = $this->authService->listUsers();

        return new JsonResponse($users);
    }

    public function login(): Response {
        // Here the user should already be logged in

        /** @var User $user */
        $user = $this->getUser();

        $token = $this->jwtService->create($user);
        $response = ['token' => $token, 'name' => $user->name, 'description' => $user->description];

        return new JsonResponse($response);
    }
}
