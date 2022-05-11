<?php
declare(strict_types=1);

namespace App\Controller;

use App\Interfaces\StationNotFoundException;
use App\Service\StationService;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\HttpFoundation\ParameterBag;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Annotation\Route;
use Symfony\Component\Validator\Constraints as Assert;
use function App\Functions\validate;

/**
 * @Route("/api/station/{stationId}/info")
 */
class StationInfoController {
    private StationService $stationService;

    public function __construct(StationService $stationService) {
        $this->stationService = $stationService;
    }

    /**
     * @Route("", methods={"POST"})
     */
    public function updateStationInfo(string $stationId, Request $request): Response {
        if (null !== ($validation = validateUpdateStationInfoRequest($request->request))) {
            return $validation;
        }

        $appVersion = $request->request->get('appVersion');
        $appVersionCode = $request->request->getInt('appVersionCode');
        $connected = $request->request->getBoolean('connected');

        try {
            $this->stationService->updateAppInfo($stationId, $appVersion, $appVersionCode, $connected);
            return new Response(null, 201);
        } catch (StationNotFoundException $e) {
            return new JsonResponse(['errors' => ['station not found']], 404);
        }
    }
}

function validateUpdateStationInfoRequest(ParameterBag $request): ?Response {
    $constraints = [
        'appVersionCode' => new Assert\PositiveOrZero(),
        'appVersion' => new Assert\NotBlank(),
        'connected' => new Assert\Type(['type' => 'bool']),
    ];

    $constraint = new Assert\Collection($constraints);

    return validate($request, $constraint);
}
