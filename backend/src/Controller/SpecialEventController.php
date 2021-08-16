<?php
declare(strict_types=1);


namespace App\Controller;


use App\Controller\Support\DateControllerTrait;
use App\Interfaces\StationNotFoundException;
use App\Service\SpecialEventService;
use Sensio\Bundle\FrameworkExtraBundle\Configuration\IsGranted;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Annotation\Route;

/**
 * @Route("/api/special")
 *
 * @IsGranted("ROLE_USER")
 */
class SpecialEventController {
    use DateControllerTrait;

    private SpecialEventService $specialEventService;

    public function __construct(SpecialEventService $specialEventService) {
        $this->specialEventService = $specialEventService;
    }

    /**
     * @Route("/{date}", methods={"GET"})
     */
    public function get(string $date): Response {
        $date = $this->checkDate($date);

        return new JsonResponse($this->specialEventService->get($date));
    }

    /**
     * @Route("/{date}/{stationId}", methods={"GET"})
     */
    public function getByStation(string $date, string $stationId): Response {
        $date = $this->checkDate($date);

        try {
            return new JsonResponse($this->specialEventService->getByStation($date, $stationId));
        } catch (StationNotFoundException $e) {
            return new JsonResponse(['errors' => ['station not found']], 404);
        }
    }

}
