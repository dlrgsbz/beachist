<?php
declare(strict_types=1);


namespace App\Controller;

use App\Interfaces\StationNotFoundException;
use App\Service\SpecialEventService;
use DateTime;
use Symfony\Component\HttpFoundation\InputBag;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Annotation\Route;
use Symfony\Component\Validator\Constraints as Assert;
use function App\Functions\validate;

const DATE_FORMAT = 'Y-m-d\TH:i:sP';

/**
 * @Route("/api/station/{stationId}/special")
 */
class StationSpecialEventController {
    private SpecialEventService $specialEventService;

    public function __construct(SpecialEventService $specialEventService) {
        $this->specialEventService = $specialEventService;
    }


    /**
     * @Route("", methods={"POST"})
     */
    function create(Request $request, string $stationId): Response {
        if (null !== ($validation = validateCreateSpecialEventRequest($request->request))) {
            return $validation;
        }

        $note = $request->request->get('note');
        $date = $request->request->get('date');
        $date = $date ? DateTime::createFromFormat(DATE_FORMAT, $date) : null;

        try {
            $id = $this->specialEventService->create($stationId, $note, $date);
        } catch (StationNotFoundException $e) {
            return new JsonResponse(['errors' => ['station not found']], 404);
        }

        return new JsonResponse(['id' => $id], 201);
    }

}

function validateCreateSpecialEventRequest(InputBag $request): ?Response {
    $constraints = [];

    if ($request->get('date') !== null) {
        $constraints['date'] = new Assert\DateTime(['format' => DATE_FORMAT]);
    }

    $constraints['note'] = new Assert\NotBlank();

    $constraint = new Assert\Collection($constraints);

    return validate($request, $constraint);
}
