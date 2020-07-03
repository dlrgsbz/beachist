<?php
declare(strict_types=1);


namespace App\Repository;


use App\Entity\Tablet;
use App\Interfaces\TabletReader;
use App\Interfaces\TabletWriter;
use Doctrine\ORM\EntityManagerInterface;
use Doctrine\ORM\EntityRepository;
use Doctrine\ORM\Mapping\ClassMetadata;
use Doctrine\ORM\OptimisticLockException;
use Doctrine\ORM\ORMException;

class TabletRepository extends EntityRepository implements TabletReader, TabletWriter {
    public function __construct(EntityManagerInterface $em) {
        parent::__construct($em, new ClassMetadata(Tablet::class));
    }

    function getTablets(): array {
        return $this->findAll();
    }

    function getTablet(string $id): ?Tablet {
        $tablet = $this->findOneBy(['id' => $id]);
        if (!$tablet instanceof Tablet) {
            return null;
        }
        return $tablet;
    }

    /**
     * @throws ORMException
     * @throws OptimisticLockException
     */
    function createTablet(Tablet $tablet): string {
        $this->_em->persist($tablet);
        $this->_em->flush();

        return $tablet->id->toString();
    }
}
