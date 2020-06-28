<?php
declare(strict_types=1);


namespace App\Controller\Support;


use Symfony\Component\HttpKernel\Exception\NotFoundHttpException;

trait DateControllerTrait {
    public function checkDate(string $date): \DateTimeInterface {
        $date = \DateTime::createFromFormat('Y-m-d', $date);

        if ($date) {
            return $date;
        }

        throw new NotFoundHttpException("Date does not exist");
    }
}
