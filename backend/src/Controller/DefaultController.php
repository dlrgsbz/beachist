<?php
declare(strict_types=1);


namespace App\Controller;

use Symfony\Component\ErrorHandler\Exception\FlattenException;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\HttpKernel\Exception\NotFoundHttpException;
use Symfony\Component\HttpKernel\KernelInterface;
use Symfony\Component\Routing\Annotation\Route;

class DefaultController {
    private KernelInterface $appKernel;

    public function __construct(KernelInterface $appKernel) {
        $this->appKernel = $appKernel;
    }

    /**
     * @Route("/{frontendRoute}", defaults={"frontendRoute": null})
     */
    public function frontendAction(): Response {
        try {
            $frontend = file_get_contents($this->appKernel->getProjectDir() . '/index.html');

            return new Response($frontend, 200, ['Content-Type' => 'text/html']);
        } catch (\Exception $_) {
            throw new NotFoundHttpException();
        }
    }

    public function error(FlattenException $exception): Response {
        $statusCode = $exception->getStatusCode();

        if ($statusCode === 404) {
            $frontend = file_get_contents($this->appKernel->getProjectDir() . '/index.html');

            return new Response($frontend, 404, ['Content-Type' => 'text/html']);
        } else {
            return new Response('Es ist ein Fehler aufgetreten', $statusCode, ['Content-Type' => 'text/html']);
        }
    }
}
