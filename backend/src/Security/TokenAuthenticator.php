<?php
declare(strict_types=1);


namespace App\Security;


use App\Service\JwtService;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Security\Core\Authentication\Token\TokenInterface;
use Symfony\Component\Security\Core\Exception\AuthenticationException;
use Symfony\Component\Security\Core\Exception\CustomUserMessageAuthenticationException;
use Symfony\Component\Security\Http\Authenticator\AbstractAuthenticator;
use Symfony\Component\Security\Http\Authenticator\Passport\PassportInterface;
use Symfony\Component\Security\Http\Authenticator\Passport\SelfValidatingPassport;

class TokenAuthenticator extends AbstractAuthenticator {
    private JwtService $jwtService;

    public function __construct(JwtService $jwtService) {
        $this->jwtService = $jwtService;
    }

    public function supports(Request $request): ?bool {
        return $request->headers->has('Authorization');
    }

    public function authenticate(Request $request): PassportInterface {
        $token = $request->headers->get('Authorization');

        if (null === $token) {
            throw new CustomUserMessageAuthenticationException('No token provided');
        }

        if (!preg_match('/Bearer\s(\S+)/', $token, $matches)) {
            throw new CustomUserMessageAuthenticationException('No token provided');
        }

        $token = $matches[1];

        if (null === $token) {
            throw new CustomUserMessageAuthenticationException('No token provided');
        }

        try {
            $user = $this->jwtService->validate($token);

            return new SelfValidatingPassport($user);
        } catch (\Exception $e) {
            throw new CustomUserMessageAuthenticationException('Invalid token provided');
        }
    }

    public function onAuthenticationSuccess(Request $request, TokenInterface $token, string $firewallName): ?Response {
        return null;
    }

    public function onAuthenticationFailure(Request $request, AuthenticationException $exception): ?Response {
        return new Response(null, Response::HTTP_UNAUTHORIZED);
    }
}
