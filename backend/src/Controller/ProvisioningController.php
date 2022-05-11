<?php
declare(strict_types=1);


namespace App\Controller;


use App\Interfaces\StationNotFoundException;
use Sensio\Bundle\FrameworkExtraBundle\Configuration\IsGranted;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Annotation\Route;
use App\Service\StationService;

/**
 * @Route("/api/provision")
 */
class ProvisioningController {
    private StationService $stationService;

    public function __construct(StationService $stationService) {
        $this->stationService = $stationService;
    }

   /**
    * @Route("/{stationId}", methods={"POST"})
    * 
    * @IsGranted("ROLE_ADMIN")
    */ 
    public function createProvisioningAction(string $stationId): Response {
        try {
            $provisioning = $this->stationService->createProvisioning($stationId);
            return new JsonResponse($provisioning, 201);
        } catch (StationNotFoundException $e) {
            return new JsonResponse(['errors' => ['station not found']], 404);
        }
    }

    /**
     * @Route("", methods={"GET"})
     * 
     * @IsGranted("ROLE_ADMIN")
     */
    public function getProvisionRequests(): Response {
        $provisions = $this->stationService->listProvisions();

        return new JsonResponse($provisions);
    }

    /**
     * @Route("", methods={"POST"})
     */
    public function provisionDeviceAction(Request $request): Response {
        $username = $request->headers->get('php-auth-user');
        $password = $request->headers->get('php-auth-pw');

        if ($username !== 'beachist') {
            return new JsonResponse(['errors' => ['invalid user']], 401);
        }

        try {
            $stationId = $this->stationService->provisionDevice($password);
            return new JsonResponse(['stationId' => $stationId]);
        } catch (StationNotFoundException $e) {
            return new JsonResponse(['errors' => ['invalid password'], 401]);
        }
    }
}
