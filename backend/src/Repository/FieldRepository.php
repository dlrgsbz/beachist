<?php
declare(strict_types=1);

namespace App\Repository;


use App\Entity\Field;
use App\Interfaces\FieldReader;
use App\Interfaces\FieldWriter;
use Doctrine\ORM\EntityManagerInterface;
use Doctrine\ORM\EntityRepository;
use Doctrine\ORM\Mapping\ClassMetadata;

class FieldRepository extends EntityRepository implements FieldWriter, FieldReader {
    public function __construct(EntityManagerInterface $em) {
        parent::__construct($em, new ClassMetadata(Field::class));
    }

    function getAll(): array {
        return $this->createQueryBuilder('f')
            ->addSelect('CASE WHEN f.sortId IS NULL THEN 1 ELSE 0 END AS HIDDEN sortIdIsNull')
            ->where('f.deleted = false')
            ->orderBy('sortIdIsNull', 'ASC')
            ->addOrderBy('f.sortId', 'ASC')
            ->addOrderBy('f.name', 'ASC')
            ->getQuery()
            ->getResult();
    }

    function get(string $id): ?Field {
        $field = $this->findOneBy(['id' => $id, 'deleted' => false]);
        if (!$field instanceof Field) {
            return null;
        }
        return $field;
    }

    function create(Field $field): string {
        $this->_em->persist($field);
        $this->_em->flush();

        return $field->id;
    }

    function update(Field $field): void {
        $this->_em->persist($field);
        $this->_em->flush();
    }

    function softDelete(Field $field): void {
        $field->deleted = true;
        $this->_em->persist($field);
        $this->_em->flush();
    }

    function reorder(array $orderedIds): void {
        $position = 10;
        foreach ($orderedIds as $id) {
            $field = $this->findOneBy(['id' => $id, 'deleted' => false]);
            if ($field instanceof Field) {
                $field->sortId = $position;
                $this->_em->persist($field);
            }
            $position += 10;
        }
        $this->_em->flush();
    }
}
