<?php
declare(strict_types=1);


namespace App\Controller;

use App\Entity\StateKind;
use App\Interfaces\FieldNotFoundException;
use App\Interfaces\StationNotFoundException;
use App\Service\EntryService;
use Symfony\Component\HttpFoundation\InputBag;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Annotation\Route;
use Symfony\Component\Validator\Constraints as Assert;
use function App\Functions\validate;

/**
 * @Route("/api/station/{stationId}/field/{fieldId}/entry")
 */
class StationEntryController {
    private EntryService $entryService;

    public function __construct(EntryService $entryService) {
        $this->entryService = $entryService;
    }

    /**
     * @Route("", methods={"POST"})
     */
    public function create(Request $request, string $stationId, string $fieldId): Response {
        if (null !== ($validation = validateCreateEntryRequest($request->request))) {
            return $validation;
        }

        $state = $request->request->getBoolean('state', false);
        $stateKind = !$state ? StateKind::make($request->request->get('stateKind')) : null;
        $amount = $request->request->get('amount');
        $amount = $amount ? (int)$amount : null;
        $note = $request->request->get('note');
        $crew = $request->request->get('crew');

        try {
            $id = $this->entryService->create($stationId, $fieldId, $state, $stateKind, $amount, $note, $crew);
        } catch (FieldNotFoundException $e) {
            return new JsonResponse(['errors' => ['field not found']], 404);
        } catch (StationNotFoundException $e) {
            return new JsonResponse(['errors' => ['station not found']], 404);
        }

        return new JsonResponse(['id' => $id], 201);
    }
}

function validateCreateEntryRequest(InputBag $request): ?Response {
    $constraints = [
        'state' => new Assert\AtLeastOneOf([
            new Assert\IsTrue(),
            new Assert\IsFalse(),
        ]),
    ];

    if ($request->get('state') === false) {
        $constraints['stateKind'] = new Assert\Regex(['pattern' => '/^(broken|tooLittle|other)$/']);
    }

    if ($request->get('stateKind') === 'tooLittle') {
        $constraints['amount'] = new Assert\PositiveOrZero();
    }

    if ($request->get('stateKind') === 'other' || $request->get('stateKind') === 'broken' || $request->get('note')) {
        $constraints['note'] = new Assert\NotBlank();
    }

    if ($request->get('crew') !== null) {
        $constraints['crew'] = new Assert\NotBlank();
    }

    $constraint = new Assert\Collection($constraints);

    return validate($request, $constraint);
}
