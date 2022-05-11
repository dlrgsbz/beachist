<?php
declare(strict_types=1);


namespace App\Repository;


use App\Entity\AppInfo;
use App\Entity\Station;
use App\Interfaces\AppInfoReader;
use App\Interfaces\AppInfoWriter;
use Doctrine\ORM\EntityManagerInterface;
use Doctrine\ORM\EntityRepository;
use Doctrine\ORM\Mapping\ClassMetadata;
use Exception;

class AppInfoRepository extends EntityRepository implements AppInfoReader, AppInfoWriter {
    public function __construct(EntityManagerInterface $em) {
        parent::__construct($em, new ClassMetadata(AppInfo::class));
    }

    function getLatestAppInfo(string $id): ?AppInfo {
        $result = $this->createQueryBuilder('v')
            ->orderBy('v.date', 'DESC')
            ->setMaxResults(1)
            ->where('v.station = :stationId')
            ->setParameter('stationId', $id)
            ->getQuery();

        try {
            return $result->getOneOrNullResult();
        } catch (Exception $_) {
            return null;
        }
    }

    /** @throws */
    function setAppInfo(Station $station, string $version, int $versionCode, bool $connected): void {
        $version = new AppInfo($station, $version, $versionCode, $connected);

        $this->_em->persist($version);
        $this->_em->flush();
    }
}
