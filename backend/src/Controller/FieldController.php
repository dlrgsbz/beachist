<?php
declare(strict_types=1);

namespace App\Controller;

use App\Interfaces\FieldNotFoundException;
use App\Service\FieldService;
use Sensio\Bundle\FrameworkExtraBundle\Configuration\IsGranted;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\HttpFoundation\ParameterBag;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\HttpKernel\Exception\NotFoundHttpException;
use Symfony\Component\Routing\Annotation\Route;
use Symfony\Component\Validator\Constraints as Assert;
use function App\Functions\validate;

/**
 * @Route("/api/field")
 *
 * @IsGranted("ROLE_USER")
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
     * @Route("/{id}", methods={"GET"}, requirements={"id"="[0-9a-fA-F\-]{36}"})
     */
    public function get(string $id): Response {
        $field = $this->fieldService->get($id);
        if (!$field) {
            throw new NotFoundHttpException("Field not found");
        }
        return new JsonResponse($field);
    }

    /**
     * @Route("", methods={"POST"})
     *
     * @IsGranted("ROLE_ADMIN")
     */
    public function create(Request $request): Response {
        $data = $request->request;
        if (null !== ($validation = validateFieldRequest($data))) {
            return $validation;
        }

        try {
            $field = $this->fieldService->create(
                $data->get('name'),
                $this->nullableInt($data, 'sortId'),
                $data->get('parent')
            );
        } catch (FieldNotFoundException $e) {
            return new JsonResponse(['errors' => ['parent field not found']], 404);
        }

        return new JsonResponse($field, 201);
    }

    /**
     * @Route("/reorder", methods={"PUT"})
     *
     * @IsGranted("ROLE_ADMIN")
     */
    public function reorder(Request $request): Response {
        $ids = $request->request->all()['ids'] ?? null;
        if (!is_array($ids) || $ids === []) {
            return new JsonResponse(['errors' => ['ids must be a non-empty array']], 400);
        }

        try {
            $fields = $this->fieldService->reorder(array_values($ids));
        } catch (FieldNotFoundException $e) {
            return new JsonResponse(['errors' => ['field not found']], 404);
        }

        return new JsonResponse($fields);
    }

    /**
     * @Route("/{id}", methods={"PUT"}, requirements={"id"="[0-9a-fA-F\-]{36}"})
     *
     * @IsGranted("ROLE_ADMIN")
     */
    public function update(string $id, Request $request): Response {
        $data = $request->request;
        if (null !== ($validation = validateFieldRequest($data))) {
            return $validation;
        }

        try {
            $field = $this->fieldService->update(
                $id,
                $data->get('name'),
                $this->nullableInt($data, 'sortId'),
                $data->get('parent')
            );
        } catch (FieldNotFoundException $e) {
            return new JsonResponse(['errors' => ['field not found']], 404);
        }

        return new JsonResponse($field);
    }

    /**
     * @Route("/{id}", methods={"DELETE"}, requirements={"id"="[0-9a-fA-F\-]{36}"})
     *
     * @IsGranted("ROLE_ADMIN")
     */
    public function delete(string $id): Response {
        try {
            $this->fieldService->delete($id);
        } catch (FieldNotFoundException $e) {
            return new JsonResponse(['errors' => ['field not found']], 404);
        }

        return new Response(null, 204);
    }

    private function nullableInt(ParameterBag $data, string $key): ?int {
        $value = $data->get($key);
        if ($value === null || $value === '') {
            return null;
        }
        return (int) $value;
    }
}

function validateFieldRequest(ParameterBag $request): ?Response {
    $constraints = [
        'name' => new Assert\NotBlank(),
    ];

    if ($request->get('sortId') !== null && $request->get('sortId') !== '') {
        $constraints['sortId'] = new Assert\Type(['type' => 'numeric']);
    }

    if ($request->get('parent') !== null && $request->get('parent') !== '') {
        $constraints['parent'] = new Assert\Uuid();
    }

    $constraint = new Assert\Collection([
        'fields' => $constraints,
        'allowExtraFields' => true,
    ]);

    return validate($request, $constraint);
}
