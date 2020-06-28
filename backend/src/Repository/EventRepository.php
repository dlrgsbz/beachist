<?php
declare(strict_types=1);


namespace App\Repository;


use App\Entity\DailyStats;
use App\Entity\Event;
use App\Interfaces\EventReader;
use App\Interfaces\EventWriter;
use App\Types\EventType;
use DateTimeInterface;
use Doctrine\ORM\EntityManagerInterface;
use Doctrine\ORM\EntityRepository;
use Doctrine\ORM\Mapping\ClassMetadata;
use Doctrine\ORM\NonUniqueResultException;
use Doctrine\ORM\Query\ResultSetMappingBuilder;

class EventRepository extends EntityRepository implements EventReader, EventWriter {
    public function __construct(EntityManagerInterface $em) {
        parent::__construct($em, new ClassMetadata(EventType::class));
    }

    /**
     * @throws
     */
    function create(Event $event): int {
        $this->_em->persist($event);
        $this->_em->flush();

        return $event->getId();
    }

    function get(\DateTimeInterface $date): DailyStats {
        $rsm = new ResultSetMappingBuilder($this->_em);
        $rsm->addScalarResult('firstAid', 'firstAid')
            ->addScalarResult('search', 'search')
            ->addScalarResult('date', 'date');


        try {
            $result = $this->_em->createNativeQuery(<<<__SQL__
SELECT CAST(fa.count AS UNSIGNED) as firstAid, CAST(se.count AS UNSIGNED) as search, :date AS date FROM
    (SELECT COUNT(1) as count FROM event WHERE type = 'firstAid' AND date BETWEEN :startDate AND :endDate) fa 
JOIN 
    (SELECT COUNT(1) as count FROM event WHERE type = 'search' AND date BETWEEN :startDate AND :endDate) se 
ON 1=1
__SQL__
                , $rsm)
                ->setParameter('startDate', $date->format('Y-m-d') . 'T00:00:00Z')
                ->setParameter('endDate', $date->format('Y-m-d') . 'T23:59:59Z')
                ->setParameter('date', $date->format('Y-m-d'))
                ->getOneOrNullResult();
        } catch (NonUniqueResultException $e) {
            return DailyStats::empty($date->format('Y-m-d'));
        }

        return new DailyStats($result);
    }


    function getByStation(DateTimeInterface $date, string $stationId): DailyStats {
        $rsm = new ResultSetMappingBuilder($this->_em);
        $rsm->addScalarResult('firstAid', 'firstAid')
            ->addScalarResult('search', 'search')
            ->addScalarResult('date', 'date');

        try {
            $result = $this->_em->createNativeQuery(<<<__SQL__
SELECT CAST(fa.count AS UNSIGNED) as firstAid, CAST(se.count AS UNSIGNED) as search, :date AS date FROM
    (SELECT COUNT(1) as count FROM event WHERE type = 'firstAid' AND date BETWEEN :startDate AND :endDate AND station_id = :stationId) fa 
JOIN 
    (SELECT COUNT(1) as count FROM event WHERE type = 'search' AND date BETWEEN :startDate AND :endDate AND station_id = :stationId) se 
ON 1=1
__SQL__
                , $rsm)
                ->setParameter('startDate', $date->format('Y-m-d') . 'T00:00:00Z')
                ->setParameter('endDate', $date->format('Y-m-d') . 'T23:59:59Z')
                ->setParameter('date', $date->format('Y-m-d'))
                ->setParameter('stationId', $stationId)
                ->getOneOrNullResult();
        } catch (NonUniqueResultException $e) {
            return DailyStats::empty($date->format('Y-m-d'));
        }

        return new DailyStats($result);
    }

}
