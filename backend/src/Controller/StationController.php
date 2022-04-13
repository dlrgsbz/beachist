<?php
declare(strict_types=1);

namespace App\Controller;


use App\Service\StationService;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\HttpKernel\Exception\NotFoundHttpException;
use Symfony\Component\Routing\Annotation\Route;

/**
 * @Route("/api/station")
 */
class StationController {
    private StationService $stationService;

    public function __construct(StationService $stationService) {
        $this->stationService = $stationService;
    }

    /**
     * @Route("", methods={"GET"})
     */
    public function getStations(): Response {
        return new JsonResponse($this->stationService->getStations());
    }

    /**
     * @Route("/info", methods={"GET"})
     */
    public function getVersions(): Response {
        return new JsonResponse($this->stationService->getLatestInfoMap());
    }

    /**
     * @Route("/{id}", methods={"GET"})
     */
    public function getStation(string $id): Response {
        $station = $this->stationService->getStation($id);
        if (!$station) {
            throw new NotFoundHttpException("Station not found");
        }
        return new JsonResponse($station);
    }

    /**
     * @Route("/{id}/field", methods={"GET"})
     */
    public function getFields(string $id): Response {
        return new JsonResponse($this->stationService->getFields($id));
    }

    /**
     * @Route("/{stationId}/field/{fieldId}", methods={"GET"})
     */
    public function getField(string $stationId, string $fieldId): Response {
        $stationField = $this->stationService->getField($stationId, $fieldId);
        if (!$stationField) {
            throw new NotFoundHttpException("Station and field not found");
        }
        return new JsonResponse($stationField);
    }
}
