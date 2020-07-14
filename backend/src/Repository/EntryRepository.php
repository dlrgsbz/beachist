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

class EntryRepository extends EntityRepository implements EntryReader, EntryWriter {
    public function __construct(EntityManagerInterface $em) {
        parent::__construct($em, new ClassMetadata(Entry::class));
    }

    function get(DateTimeInterface $date): array {
        $results = $this->createQueryBuilder('e')
            ->where('e.date BETWEEN :startDate AND :endDate')
            ->orderBy('e.date', 'ASC')
            ->setParameter('startDate', $date->format('Y-m-d') . 'T00:00:00Z')
            ->setParameter('endDate', $date->format('Y-m-d') . 'T23:59:59Z')
            ->getQuery()
            ->execute();

        $ret = [];
        foreach ($results as $result) {
            /** @var $result Entry */
            if (!isset($ret[$result->station->id])) {
                $ret[$result->station->id] = [];
            }
            $ret[$result->station->id][$result->field->id] = $result;
        }

        // this is basically a flatMap
        return array_merge(...array_values(array_map(fn($v) => array_values($v), $ret)));
    }

    function getByStation(DateTimeInterface $date, string $stationId): array {
        $results = $this->createQueryBuilder('e')
            ->where('e.date BETWEEN :startDate AND :endDate')
            ->andWhere('e.station = :stationId')
            ->orderBy('e.date', 'ASC')
            ->setParameter('startDate', $date->format('Y-m-d') . 'T00:00:00Z')
            ->setParameter('endDate', $date->format('Y-m-d') . 'T23:59:59Z')
            ->setParameter('stationId', $stationId)
            ->getQuery()
            ->execute();

        $ret = [];
        foreach ($results as $result) {
            /** @var $result Entry */
            $ret[$result->field->id] = $result;
        }

        return array_values($ret);
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
