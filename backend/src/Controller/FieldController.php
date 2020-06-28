<?php
declare(strict_types=1);

namespace App\Controller;

use App\Service\FieldService;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\HttpKernel\Exception\NotFoundHttpException;
use Symfony\Component\Routing\Annotation\Route;

/**
 * @Route("/api/field")
 */
class FieldController {
    private FieldService $fieldService;

    /**
     * FieldController constructor.
     *
     * @param FieldService $fieldService
     */
    public function __construct(FieldService $fieldService) {
        $this->fieldService = $fieldService;
    }

    /**
     * @Route("", methods={"GET"})
     */
    public function getAll(): Response {
        return new JsonResponse($this->fieldService->getAll());
    }

    /**
     * @Route("/{id}", methods={"GET"})
     */
    public function get(string $id): Response {
        $field = $this->fieldService->get($id);
        if (!$field) {
            throw new NotFoundHttpException("Field not found");
        }
        return new JsonResponse($field);
    }
}
