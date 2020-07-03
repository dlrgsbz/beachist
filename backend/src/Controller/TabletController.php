<?php
declare(strict_types=1);


namespace App\Controller;


use App\Service\TabletAlreadyExistsException;
use App\Service\TabletService;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\InputBag;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\HttpKernel\Exception\NotFoundHttpException;
use Symfony\Component\Routing\Annotation\Route;
use Symfony\Component\Routing\Generator\UrlGeneratorInterface;
use Symfony\Component\Validator\Constraints as Assert;
use function App\Functions\validate;

/**
 * @Route("/api/tablet")
 */
class TabletController extends AbstractController {
    private TabletService $tabletService;

    public function __construct(TabletService $tabletService) {
        $this->tabletService = $tabletService;
    }

    /**
     * @Route("", methods={"GET"})
     */
    public function list(): Response {
        return new JsonResponse($this->tabletService->getTablets());
    }

    /**
     * @Route("/{id}", methods={"GET"})
     */
    public function getTablet(string $id): Response {
        $tablet = $this->tabletService->getTablet($id);
        if (!$tablet) {
            throw new NotFoundHttpException();
        }
        return new JsonResponse($tablet);
    }

    /**
     * @Route("", methods={"POST"})
     */
    public function create(Request $request): Response {
        if (null !== ($validation = validateCreateTabletRequest($request->request))) {
            return $validation;
        }

        $id = $request->request->get('id');
        $name = $request->request->get('name');

        try {
            $id = $this->tabletService->create($id, $name);
        } catch (TabletAlreadyExistsException $e) {
            return new JsonResponse(['errors' => ['field not found']], 409, [
                'Location' => $this->generateUrl('app_tablet_gettablet', ['id' => $id],
                    UrlGeneratorInterface::ABSOLUTE_URL),
            ]);
        }

        return new JsonResponse(['id' => $id], 201);

    }
}

function validateCreateTabletRequest(InputBag $request): ?Response {
    $constraints = [
        'id' => new Assert\Uuid([
            'strict' => true,
            'versions' => [4],
        ]),
        'name' => new Assert\NotBlank(),
    ];

    $constraint = new Assert\Collection($constraints);

    return validate($request, $constraint);
}
