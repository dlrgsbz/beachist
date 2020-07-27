<?php
declare(strict_types=1);


namespace App\Repository;


use App\Entity\SpecialEvent;
use App\Interfaces\SpecialEventReader;
use App\Interfaces\SpecialEventWriter;
use DateTimeInterface;
use Doctrine\ORM\EntityManagerInterface;
use Doctrine\ORM\EntityRepository;
use Doctrine\ORM\Mapping\ClassMetadata;
use Ramsey\Uuid\UuidInterface;

class SpecialEventRepository extends EntityRepository implements SpecialEventReader, SpecialEventWriter {
    public function __construct(EntityManagerInterface $em) {
        parent::__construct($em, new ClassMetadata(SpecialEvent::class));
    }

    /** @throws */
    function create(SpecialEvent $event): UuidInterface {
        $this->_em->persist($event);
        $this->_em->flush();

        return $event->id;
    }

    function get(DateTimeInterface $date): array {
        return $this->createQueryBuilder('se')
            ->where('se.date BETWEEN :startDate AND :endDate')
            ->join('se.station', 's')
            ->orderBy('se.date', 'ASC')
            ->addOrderBy('s.sortId', 'ASC')
            ->setParameter('startDate', $date->format('Y-m-d') . 'T00:00:00Z')
            ->setParameter('endDate', $date->format('Y-m-d') . 'T23:59:59Z')
            ->getQuery()
            ->execute();
    }

    /**
     * @return SpecialEvent[]
     */
    function getByStation(DateTimeInterface $date, string $stationId): array {
        return $this->createQueryBuilder('se')
            ->where('se.date BETWEEN :startDate AND :endDate')
            ->andWhere('se.station = :stationId')
            ->orderBy('se.date', 'ASC')
            ->setParameter('startDate', $date->format('Y-m-d') . 'T00:00:00Z')
            ->setParameter('endDate', $date->format('Y-m-d') . 'T23:59:59Z')
            ->setParameter('stationId', $stationId)
            ->getQuery()
            ->execute();
    }
}
