<?php
declare(strict_types=1);


namespace App\Controller;


use Symfony\Component\HttpFoundation\Response;
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
        $frontend = file_get_contents($this->appKernel->getProjectDir() . '/index.html');

        return new Response($frontend, 200, ['Content-Type' => 'text/html']);
    }
}
