<?php
declare(strict_types=1);

namespace App\Repository;


use App\Entity\StationField;
use App\Interfaces\StationFieldReader;
use App\Interfaces\StationFieldWriter;
use Doctrine\ORM\EntityManagerInterface;
use Doctrine\ORM\EntityRepository;
use Doctrine\ORM\Mapping;

class StationFieldRepository extends EntityRepository implements StationFieldReader, StationFieldWriter {
    public function __construct(EntityManagerInterface $em) {
        parent::__construct($em, new Mapping\ClassMetadata(StationField::class));
    }

    public function getAll(): array {
        return $this->findAll();
    }

    public function getForStation(string $stationId): array {
        $values = $this->findBy(['station' => [null, $stationId]]);
        usort($values, fn (StationField $a, StationField $b) => $a->field->sortId - $b->field->sortId);
        return $values;
    }

    function get(string $stationId, string $fieldId): ?StationField {
        $field = $this->findOneBy(['station' => [$stationId, null], 'field' => $fieldId]);
        if (!$field instanceof StationField) {
            return null;
        }
        return $field;
    }
}
