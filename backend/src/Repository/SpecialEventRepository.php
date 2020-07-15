<?php
declare(strict_types=1);


namespace App\Repository;


use App\Entity\SpecialEvent;
use App\Interfaces\SpecialEventReader;
use App\Interfaces\SpecialEventWriter;
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
}
