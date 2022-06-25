<?php
declare(strict_types=1);

namespace App\Repository;

use App\Entity\CrewInfo;
use App\Interfaces\CrewReader;
use App\Interfaces\CrewWriter;
use Doctrine\ORM\EntityManagerInterface;
use Doctrine\ORM\EntityRepository;
use Doctrine\ORM\Mapping\ClassMetadata;

const CREW_REPO_DATE_FORMAT = 'Y-m-d';

class CrewRepository extends EntityRepository implements CrewReader, CrewWriter {
    public function __construct(EntityManagerInterface $em) {
        parent::__construct($em, new ClassMetadata(CrewInfo::class));
    }

    /**
     * @throws \Doctrine\DBAL\Driver\Exception
     * @throws \Doctrine\DBAL\Exception
     */
    function upsert(CrewInfo $crewInfo): void {
        $stmt = $this->_em->getConnection()->prepare("
INSERT INTO crew_info 
    (station_id, crew, date) VALUES (:stationId, :crew, :date)
ON DUPLICATE KEY UPDATE
    crew = :crew
");

        $stmt->bindParam('stationId', $crewInfo->station->id);
        $stmt->bindParam('crew', $crewInfo->crew);
        $date = $crewInfo->date;
        $stmt->bindParam('date', $date);
        $stmt->execute();
    }

    function getCrews(\DateTime $date): array {
        return $this->findBy(['date' => $date->format(CREW_REPO_DATE_FORMAT)]);
    }
}
