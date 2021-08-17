<?php
declare(strict_types=1);


namespace App\Controller;

use App\Controller\Support\DateControllerTrait;
use App\Interfaces\StationNotFoundException;
use App\Service\EventService;
use Sensio\Bundle\FrameworkExtraBundle\Configuration\IsGranted;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Annotation\Route;

/**
 * @Route("/api/event")
 *
 * @IsGranted("ROLE_USER")
 */
class EventController {
    use DateControllerTrait;

    private EventService $eventService;

    /**
     * EventController constructor.
     *
     * @param EventService $eventService
     */
    public function __construct(EventService $eventService) {
        $this->eventService = $eventService;
    }

    /**
     * @Route("/{date}", methods={"GET"})
     */
    public function get(string $date): Response {
        $date = $this->checkDate($date);

        return new JsonResponse($this->eventService->get($date));
    }

    /**
     * @Route("/{date}/{stationId}", methods={"GET"})
     */
    public function getByStation(string $date, string $stationId): Response {
        $date = $this->checkDate($date);

        try {
            return new JsonResponse($this->eventService->getByStation($date, $stationId));
        } catch (StationNotFoundException $e) {
            return new JsonResponse(['errors' => ['station not found']], 404);
        }
    }
}
