<?php
declare(strict_types=1);

namespace App\Controller;

use App\Interfaces\FieldNotFoundException;
use App\Interfaces\StationNotFoundException;
use App\Service\StationService;
use Sensio\Bundle\FrameworkExtraBundle\Configuration\IsGranted;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Annotation\Route;

/**
 * Endpoints for managing which fields (items) are assigned to which station.
 *
 * @IsGranted("ROLE_ADMIN")
 */
class StationFieldController {
    private StationService $stationService;

    public function __construct(StationService $stationService) {
        $this->stationService = $stationService;
    }

    /**
     * @Route("/api/assignment", methods={"GET"})
     */
    public function getAssignments(): Response {
        return new JsonResponse($this->stationService->getAllAssignments());
    }

    /**
     * @Route("/api/station/{stationId}/field/{fieldId}", methods={"POST"})
     */
    public function assign(string $stationId, string $fieldId): Response {
        return $this->doAssign($fieldId, $stationId);
    }

    /**
     * @Route("/api/station/{stationId}/field/{fieldId}", methods={"DELETE"})
     */
    public function unassign(string $stationId, string $fieldId): Response {
        return $this->doUnassign($fieldId, $stationId);
    }

    /**
     * @Route("/api/field/{fieldId}/global", methods={"POST"})
     */
    public function assignGlobal(string $fieldId): Response {
        return $this->doAssign($fieldId, null);
    }

    /**
     * @Route("/api/field/{fieldId}/global", methods={"DELETE"})
     */
    public function unassignGlobal(string $fieldId): Response {
        return $this->doUnassign($fieldId, null);
    }

    /**
     * Replaces all assignments of a field in a single request.
     *
     * @Route("/api/field/{fieldId}/assignments", methods={"PUT"})
     */
    public function setAssignments(string $fieldId, Request $request): Response {
        $data = $request->request->all();
        $global = filter_var($data['global'] ?? false, FILTER_VALIDATE_BOOLEAN);
        $stations = $data['stations'] ?? [];
        if (!is_array($stations)) {
            $stations = [];
        }

        $required = null;
        if (isset($data['required']) && $data['required'] !== '' && $data['required'] !== null) {
            $required = filter_var($data['required'], FILTER_VALIDATE_INT);
            if ($required === false) {
                $required = null;
            }
        }

        $note = null;
        if (isset($data['note']) && is_string($data['note']) && trim($data['note']) !== '') {
            $note = $data['note'];
        }

        try {
            $assignments = $this->stationService->setFieldAssignments($fieldId, $global, array_values($stations), $required, $note);
            return new JsonResponse($assignments);
        } catch (StationNotFoundException $e) {
            return new JsonResponse(['errors' => ['station not found']], 404);
        } catch (FieldNotFoundException $e) {
            return new JsonResponse(['errors' => ['field not found']], 404);
        }
    }

    private function doAssign(string $fieldId, ?string $stationId): Response {
        try {
            $assignment = $this->stationService->assignField($fieldId, $stationId);
            return new JsonResponse($assignment, 201);
        } catch (StationNotFoundException $e) {
            return new JsonResponse(['errors' => ['station not found']], 404);
        } catch (FieldNotFoundException $e) {
            return new JsonResponse(['errors' => ['field not found']], 404);
        }
    }

    private function doUnassign(string $fieldId, ?string $stationId): Response {
        try {
            $this->stationService->unassignField($fieldId, $stationId);
            return new Response(null, 204);
        } catch (StationNotFoundException $e) {
            return new JsonResponse(['errors' => ['station not found']], 404);
        } catch (FieldNotFoundException $e) {
            return new JsonResponse(['errors' => ['field not found']], 404);
        }
    }
}
