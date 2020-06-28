<?php
declare(strict_types=1);


namespace App\Controller;

use App\Controller\Support\DateControllerTrait;
use App\Service\EventService;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Annotation\Route;

/**
 * @Route("/api/event")
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
     * @Route("/{date}")
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

        return new JsonResponse($this->eventService->getByStation($date, $stationId));
    }
}
