<?php
declare(strict_types=1);


namespace App\Repository;


use App\Entity\AppVersion;
use App\Entity\Station;
use App\Interfaces\VersionReader;
use App\Interfaces\VersionWriter;
use Doctrine\ORM\EntityManagerInterface;
use Doctrine\ORM\EntityRepository;
use Doctrine\ORM\Mapping\ClassMetadata;
use Exception;

class VersionRepository extends EntityRepository implements VersionReader, VersionWriter {
    public function __construct(EntityManagerInterface $em) {
        parent::__construct($em, new ClassMetadata(AppVersion::class));
    }

    function getLatestAppVersion(string $id): ?string {
        $result = $this->createQueryBuilder('v')
            ->orderBy('v.date', 'DESC')
            ->setMaxResults(1)
            ->where('v.station = :stationId')
            ->setParameter('stationId', $id)
            ->getQuery();

        try {
            return $result->getSingleScalarResult();
        } catch (Exception $_) {
            return null;
        }
    }

    /** @throws */
    function setAppVersion(Station $station, string $version) {
        $version = new AppVersion($station, $version);

        $this->_em->persist($version);
        $this->_em->flush();
    }
}
