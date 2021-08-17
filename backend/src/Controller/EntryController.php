<?php
declare(strict_types=1);


namespace App\Controller;


use App\Controller\Support\DateControllerTrait;
use App\Service\EntryService;
use Sensio\Bundle\FrameworkExtraBundle\Configuration\IsGranted;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Annotation\Route;

/**
 * @Route("/api/entry")
 */
class EntryController {
    use DateControllerTrait;

    private EntryService $entryService;

    public function __construct(EntryService $entryService) {
        $this->entryService = $entryService;
    }


    /**
     * @Route("/{date}", methods={"GET"})
     *
     * @IsGranted("ROLE_USER")
     */
    public function get(string $date): Response {
        $date = $this->checkDate($date);

        return new JsonResponse($this->entryService->get($date));
    }

    /**
     * @Route("/{date}/{stationId}", methods={"GET"})
     */
    public function getByStation(string $date, string $stationId): Response {
        $date = $this->checkDate($date);

        return new JsonResponse($this->entryService->getByStation($date, $stationId));
    }
}
