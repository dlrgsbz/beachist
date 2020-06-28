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

        return $this->_em->createNativeQuery(<<<__SQL__
SELECT * FROM (
    SELECT e.*, 
        @row_number:=CASE
        WHEN @station_id = station_id AND @field_id = field_id 
			THEN @row_number + 1
        ELSE 1
    END AS rn,
    @field_id:=field_id FieldId,
    @station_id:=station_id StationId
    FROM entry AS e
) ranked_entries WHERE date BETWEEN :startDate AND :endDate AND rn = 1;
__SQL__
            , $rsm)
            ->setParameter('startDate', $date->format('Y-m-d') . 'T00:00:00Z')
            ->setParameter('endDate', $date->format('Y-m-d') . 'T23:59:59Z')
            ->execute();
    }

    function getByStation(DateTimeInterface $date, string $stationId): array {
        $rsm = new ResultSetMappingBuilder($this->_em);
        $rsm->addRootEntityFromClassMetadata('App\Entity\Entry', 'e');

        return $this->_em->createNativeQuery(<<<__SQL__
SELECT * FROM (
    SELECT e.*, 
        @row_number:=CASE
        WHEN @station_id = station_id AND @field_id = field_id 
			THEN @row_number + 1
        ELSE 1
    END AS rn,
    @field_id:=field_id FieldId,
    @station_id:=station_id StationId
    FROM entry AS e
) ranked_entries WHERE date BETWEEN :startDate AND :endDate AND station_id = :stationId AND rn = 1;
__SQL__
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
