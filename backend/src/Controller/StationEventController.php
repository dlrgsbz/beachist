<?php
declare(strict_types=1);


namespace App\Controller;

use App\Entity\EventType;
use App\Interfaces\StationNotFoundException;
use App\Service\EventService;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\HttpFoundation\ParameterBag;
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
        $data = $request->request;
        if (null !== ($validation = validateCreateEventRequest($data))) {
            return $validation;
        }

        $type = EventType::firstAid();
        if ($data->get('date')) {
            $date = \DateTime::createFromFormat(\DateTime::ATOM, $data->get('date'));
        } else {
            $date = null;
        }

        try {
            $id = $this->eventService->create($stationId, $type, $data->get('id'), $date);
        } catch (StationNotFoundException $e) {
            return new JsonResponse(['errors' => ['station not found']], 404);
        }

        return new JsonResponse(['id' => $id], 201);

    }
}

function validateCreateEventRequest(ParameterBag $request): ?Response {
    $constraints = [
        'type' => new Assert\Regex(['pattern' => '/^(firstAid|search)$/']),
    ];

    if ($request->get('id')) {
        $constraints['id'] = new Assert\Uuid();
    }

    if ($request->get('date')) {
        $constraints['date'] = new Assert\DateTime(['format' => \DateTimeInterface::ATOM]);
    }

    $constraint = new Assert\Collection($constraints);

    return validate($request, $constraint);
}
