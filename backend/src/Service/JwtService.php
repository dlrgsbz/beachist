<?php
declare(strict_types=1);


namespace App\Service;


use App\Entity\User;
use App\Interfaces\UserReader;
use Lcobucci\Clock\SystemClock;
use Lcobucci\JWT\Configuration;
use Lcobucci\JWT\Signer\Hmac\Sha256;
use Lcobucci\JWT\Signer\Key\InMemory;
use Lcobucci\JWT\UnencryptedToken;
use Lcobucci\JWT\Validation\Constraint\SignedWith;
use Lcobucci\JWT\Validation\Constraint\StrictValidAt;
use Lcobucci\JWT\Validation\RequiredConstraintsViolated;

class JwtService {
    private Configuration $configuration;
    private UserReader $userReader;

    public function __construct(string $key, UserReader $userReader) {
        $key = InMemory::base64Encoded($key);
        $this->configuration = Configuration::forSymmetricSigner(new Sha256(), $key);
        $this->configuration->setValidationConstraints(
            new StrictValidAt(new SystemClock(new \DateTimeZone('UTC'))),
            new SignedWith($this->configuration->signer(), $this->configuration->signingKey())
        );
        $this->userReader = $userReader;
    }

    public function create(User $user): string {
        $now = new \DateTimeImmutable();
        $token = $this->configuration->builder()
            ->relatedTo((string)$user->id)
            ->issuedAt($now)
            ->expiresAt($now->modify('+1 day'))
            ->canOnlyBeUsedAfter($now)
            ->getToken($this->configuration->signer(), $this->configuration->signingKey());

        return $token->toString();
    }

    public function createTemporary(): string {
        $now = new \DateTimeImmutable();
        $token = $this->configuration->builder()
            ->relatedTo('temporary')
            ->issuedAt($now)
            ->expiresAt($now->modify('+4 hours'))
            ->canOnlyBeUsedAfter($now)
            ->withClaim('prm', ['ROLE_USER'])
            ->getToken($this->configuration->signer(), $this->configuration->signingKey());

        return $token->toString();
    }

    /**
     * @throws InvalidTokenException
     * @throws UserNotFoundException
     */
    public function validate(string $token): User {
        $parsed = $this->configuration->parser()->parse($token);
        assert($parsed instanceof UnencryptedToken);

        $constraints = $this->configuration->validationConstraints();

        try {
            $this->configuration->validator()->assert($parsed, ...$constraints);

            $uid = $parsed->claims()->get('sub');

            if (!$uid) {
                throw new UserNotFoundException();
            }

            if ($uid === 'temporary') {
                return $this->temporaryUser();
            }

            $user = $this->userReader->getById((int)$uid);

            if (!$user) {
                throw new UserNotFoundException();
            }

            return $user;
        } catch (RequiredConstraintsViolated $e) {
            // list of constraints violation exceptions:
            throw new InvalidTokenException();
        }
    }

    private function temporaryUser(): User {
        $user = new User();
        $user->name = 'temporär';
        $user->description = 'Rettungsschwimmer*in';
        $user->id = -9;
        $user->roles = ['ROLE_USER'];
        return $user;
    }
}
