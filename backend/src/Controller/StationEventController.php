<?php
declare(strict_types=1);


namespace App\Controller;

use App\Entity\EventType;
use App\Interfaces\StationNotFoundException;
use App\Service\EventService;
use Symfony\Component\HttpFoundation\InputBag;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Annotation\Route;
use Symfony\Component\Validator\Constraints as Assert;
use function App\Functions\validate;

/**
 * @Route("/api/station/{stationId}/event")
 */
class StationEventController {
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
     * @Route("", methods={"POST"})
     */
    function create(Request $request, string $stationId): Response {
        if (null !== ($validation = validateCreateEventRequest($request->request))) {
            return $validation;
        }

        $type = EventType::make($request->request->get('type'));

        try {
            $id = $this->eventService->create($stationId, $type);
        } catch (StationNotFoundException $e) {
            return new JsonResponse(['errors' => ['station not found']], 404);
        }

        return new JsonResponse(['id' => $id], 201);

    }
}

function validateCreateEventRequest(InputBag $request): ?Response {
    $constraint = new Assert\Collection([
        'type' => new Assert\Regex(['pattern' => '/^(firstAid|search)$/']),
    ]);

    return validate($request, $constraint);
}
