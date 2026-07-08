<?php
declare(strict_types=1);

namespace App\Repository;


use App\Entity\Field;
use App\Entity\Station;
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

    function getForField(string $fieldId): array {
        return $this->findBy(['field' => $fieldId]);
    }

    function findAssignment(string $fieldId, ?string $stationId): ?StationField {
        $criteria = [
            'field' => $fieldId,
            'station' => $stationId,
        ];

        $assignment = $this->findOneBy($criteria);
        if (!$assignment instanceof StationField) {
            return null;
        }
        return $assignment;
    }

    function assign(string $fieldId, ?string $stationId, ?int $required = null, ?string $note = null): StationField {
        if ($stationId === null) {
            // A global assignment supersedes any station-specific rows.
            $this->unassignAllForField($fieldId);
        }

        $existing = $this->findAssignment($fieldId, $stationId);
        if ($existing instanceof StationField) {
            $existing->required = $required;
            $existing->note = $note;
            $this->_em->flush();
            return $existing;
        }

        $assignment = new StationField();
        $assignment->field = $this->_em->getReference(Field::class, $fieldId);
        $assignment->station = $stationId === null ? null : $this->_em->getReference(Station::class, $stationId);
        $assignment->required = $required;
        $assignment->note = $note;

        $this->_em->persist($assignment);
        $this->_em->flush();

        return $assignment;
    }

    function unassign(string $fieldId, ?string $stationId): void {
        $assignment = $this->findAssignment($fieldId, $stationId);
        if (!$assignment instanceof StationField) {
            return;
        }

        $this->_em->remove($assignment);
        $this->_em->flush();
    }

    function unassignAllForField(string $fieldId): void {
        $assignments = $this->findBy(['field' => $fieldId]);
        foreach ($assignments as $assignment) {
            $this->_em->remove($assignment);
        }
        $this->_em->flush();
    }
}
