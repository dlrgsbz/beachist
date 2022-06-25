<?php
declare(strict_types=1);

namespace App\Controller;

use App\Service\CrewService;
use DateTime;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\HttpFoundation\ParameterBag;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Annotation\Route;
use Symfony\Component\Validator\Constraints as Assert;
use function App\Functions\validate;

const GET_CREW_DATE_FORMAT = 'Y-m-d';

/**
 * @Route("/api/crew/")
 */
class CrewController {
    private CrewService $crewService;

    public function __construct(CrewService $crewService) {
        $this->crewService = $crewService;
    }

    /**
     * @Route("{date}", methods={"GET"})
     */
    public function getCrews(string $date): Response {
        $parameterBag = new ParameterBag(['date' => $date]);
        if (null !== ($validation = validateGetCrewRequest($parameterBag))) {
            return $validation;
        }

        $date = DateTime::createFromFormat(GET_CREW_DATE_FORMAT, $date);

        $crews = $this->crewService->getCrews($date);

        return new JsonResponse($crews);
    }

}

function validateGetCrewRequest(ParameterBag $request): ?Response {
    $constraints = [];

    $constraints['date'] = new Assert\DateTime(['format' => GET_CREW_DATE_FORMAT]);

    $constraint = new Assert\Collection($constraints);

    return validate($request, $constraint);
}
