<?php
declare(strict_types=1);


namespace App\Controller;

use App\Entity\User;
use Sensio\Bundle\FrameworkExtraBundle\Configuration\IsGranted;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Annotation\Route;

/**
 * @Route("/api/me")
 *
 * @IsGranted("ROLE_USER")
 */
class MeController extends AbstractController {
    /**
     * @Route("", methods={"GET"})
     */
    public function currentUserAction(): Response {
        /** @var User $user */
        $user = $this->getUser();

        return new JsonResponse($user->toDto());
    }
}
