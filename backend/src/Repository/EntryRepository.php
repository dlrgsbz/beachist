<?php
declare(strict_types=1);


namespace App\Repository;


use App\Entity\Entry;
use App\Interfaces\EntryReader;
use App\Interfaces\EntryWriter;
use DateTimeInterface;
use Doctrine\ORM\EntityManagerInterface;
use Doctrine\ORM\EntityRepository;
use Doctrine\ORM\Mapping\ClassMetadata;
use Doctrine\ORM\OptimisticLockException;
use Doctrine\ORM\ORMException;
use Doctrine\ORM\Query\ResultSetMappingBuilder;

class EntryRepository extends EntityRepository implements EntryReader, EntryWriter {
    public function __construct(EntityManagerInterface $em) {
        parent::__construct($em, new ClassMetadata(Entry::class));
    }

    function get(DateTimeInterface $date): array {
        $rsm = new ResultSetMappingBuilder($this->_em);
        $rsm->addRootEntityFromClassMetadata('App\Entity\Entry', 'e');

        return $this->_em->createNativeQuery(
            "SELECT DISTINCT ON (station_id, field_id) * FROM entry WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC;",
            $rsm)
            ->setParameter('startDate', $date->format('Y-m-d') . 'T00:00:00Z')
            ->setParameter('endDate', $date->format('Y-m-d') . 'T23:59:59Z')
            ->execute();
    }

    function getByStation(DateTimeInterface $date, string $stationId): array {
        $rsm = new ResultSetMappingBuilder($this->_em);
        $rsm->addRootEntityFromClassMetadata('App\Entity\Entry', 'e');

        return $this->_em->createNativeQuery(
            "SELECT DISTINCT ON (station_id, field_id) * FROM entry WHERE station_id = :stationId AND date BETWEEN :startDate AND :endDate ORDER BY date DESC;"
            , $rsm)
            ->setParameter('startDate', $date->format('Y-m-d') . 'T00:00:00Z')
            ->setParameter('endDate', $date->format('Y-m-d') . 'T23:59:59Z')
            ->setParameter('stationId', $stationId)
            ->execute();
    }

    /**
     * @throws ORMException
     * @throws OptimisticLockException
     */
    function create(Entry $entry): string {
        $this->_em->persist($entry);
        $this->_em->flush();

        return $entry->id->toString();
    }
}
