<?php
declare(strict_types=1);


namespace App\Repository;


use App\Entity\Station;
use App\Entity\StationProvisioningRequest;
use Doctrine\ORM\EntityManagerInterface;
use Doctrine\ORM\EntityRepository;
use Doctrine\ORM\Mapping\ClassMetadata;

class ProvisioningRepository extends EntityRepository {
    public function __construct(EntityManagerInterface $em) {
        parent::__construct($em, new ClassMetadata(StationProvisioningRequest::class));
    }

    public function createProvisioning(Station $station, string $password): StationProvisioningRequest {
        $request = new StationProvisioningRequest($station, $password);

        $this->_em->persist($request);
        $this->_em->flush();

        return $request;
    }

    public function getProvisioning(string $password): ?StationProvisioningRequest {
        return $this->findOneBy(['password' => $password]);
    }

    public function getUnexpiredProvisions(): array {
        return $this->_em
        ->createQuery('SELECT p FROM App:StationProvisioningRequest p WHERE p.expiresAt >= CURRENT_DATE() AND p.active = true')
        ->getResult();
    }
}
