<?php
declare(strict_types=1);

namespace App\Functions;

use Symfony\Component\HttpFoundation\InputBag;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Validator\Constraint;
use Symfony\Component\Validator\Constraints\GroupSequence;
use Symfony\Component\Validator\Validation;

if (!function_exists('App\Functions\validate')) {

    function validate(InputBag $bag, Constraint $constraint, GroupSequence $groups = null): ?Response {
        if (!$groups) {
            $groups = new GroupSequence(['Default']);
        }

        $validator = Validation::createValidator();

        $violations = $validator->validate($bag->all(), $constraint, $groups);

        if ($violations->count() === 0) {
            return null;
        }

        $response = ['errors' => []];
        foreach ($violations as $violation) {
            $property = str_replace(['[', ']'], '', $violation->getPropertyPath());
            $response['errors'][$property] = $violation->getMessage();
        }

        return new JsonResponse($response, 400);
    }

}
