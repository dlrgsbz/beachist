<?php
declare(strict_types=1);

namespace App\Controller;

use App\Interfaces\StationNotFoundException;
use App\Service\CrewService;
use DateTime;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\HttpFoundation\ParameterBag;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Validator\Constraints as Assert;
use Symfony\Component\Routing\Annotation\Route;
use function App\Functions\validate;

const CREW_DATE_FORMAT = 'Y-m-d';

/**
 * @Route("/api/station/{stationId}/crew")
 */
class StationCrewController {
    private CrewService $crewService;

    public function __construct(CrewService $crewService) {
        $this->crewService = $crewService;
    }

    /**
     * @Route("", methods={"POST"})
     */
    function create(Request $request, string $stationId): Response {
        if (null !== ($validation = validateCreateCrewRequest($request->request))) {
            return $validation;
        }

        $crew = $request->request->get('crew');
        $date = $request->request->get('date');
        $date = DateTime::createFromFormat(CREW_DATE_FORMAT, $date);

        try {
            $this->crewService->addCrew($stationId, $crew, $date);
        } catch (StationNotFoundException $e) {
            return new JsonResponse(['errors' => ['station not found']], 404);
        }

        return new Response('', 204);
    }
}

function validateCreateCrewRequest(ParameterBag $request): ?Response {
    $constraints = [];

    $constraints['date'] = new Assert\DateTime(['format' => CREW_DATE_FORMAT]);

    $constraints['crew'] = new Assert\NotBlank();

    $constraint = new Assert\Collection($constraints);

    return validate($request, $constraint);
}
