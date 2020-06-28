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
        return $this->findAll();
    }

    function get(string $id): ?Field {
        $field = $this->findOneBy(['id' => $id]);
        if (!$field instanceof Field) {
            return null;
        }
        return $field;
    }
}
