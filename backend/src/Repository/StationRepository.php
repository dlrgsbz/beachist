<?php
declare(strict_types=1);

namespace App\Repository;


use App\Entity\Station;
use App\Interfaces\StationReader;
use App\Interfaces\StationWriter;
use Doctrine\ORM\EntityManagerInterface;
use Doctrine\ORM\EntityRepository;
use Doctrine\ORM\Mapping\ClassMetadata;

class StationRepository extends EntityRepository implements StationReader, StationWriter {
    public function __construct(EntityManagerInterface $em) {
        parent::__construct($em, new ClassMetadata(Station::class));
    }

    /**
     * @return Station[]
     */
    function getStations(): array {
        return $this->findBy([], ['sortId' => 'asc']);
    }

    function getStation(string $id): ?Station {
        $station = $this->findOneBy(['id' => $id]);
        if (!$station instanceof Station) {
            return null;
        }
        return $station;
    }
}
