<?php
declare(strict_types=1);

namespace App\Service;


use App\Entity\Field;
use App\Interfaces\FieldNotFoundException;
use App\Interfaces\FieldReader;
use App\Interfaces\FieldWriter;
use App\Interfaces\StationFieldWriter;
use Ramsey\Uuid\Uuid;

class FieldService {
    private FieldReader $fieldReader;
    private FieldWriter $fieldWriter;
    private StationFieldWriter $stationFieldWriter;

    public function __construct(FieldReader $fieldReader, FieldWriter $fieldWriter, StationFieldWriter $stationFieldWriter) {
        $this->fieldReader = $fieldReader;
        $this->fieldWriter = $fieldWriter;
        $this->stationFieldWriter = $stationFieldWriter;
    }

    /**
     * @return Field[]
     */
    public function getAll(): array {
        return $this->fieldReader->getAll();
    }

    public function get(string $id): ?Field {
        return $this->fieldReader->get($id);
    }

    /**
     * @throws FieldNotFoundException
     */
    public function create(string $name, ?int $sortId, ?string $parentId): Field {
        $field = new Field(Uuid::uuid4()->toString(), $name);
        $field->sortId = $sortId ?? $this->nextSortId();
        $field->parent = $this->resolveParent($parentId);
        $field->deleted = false;

        $this->fieldWriter->create($field);

        return $field;
    }

    /**
     * Reorders the (non-deleted) fields to match the given sequence of ids.
     *
     * @param string[] $orderedIds
     *
     * @return Field[]
     * @throws FieldNotFoundException
     */
    public function reorder(array $orderedIds): array {
        foreach ($orderedIds as $id) {
            if (!$this->fieldReader->get($id)) {
                throw new FieldNotFoundException();
            }
        }

        $this->fieldWriter->reorder($orderedIds);

        return $this->getAll();
    }

    private function nextSortId(): int {
        $max = 0;
        foreach ($this->fieldReader->getAll() as $field) {
            if ($field->sortId !== null && $field->sortId > $max) {
                $max = $field->sortId;
            }
        }
        return $max + 10;
    }

    /**
     * @throws FieldNotFoundException
     */
    public function update(string $id, string $name, ?int $sortId, ?string $parentId): Field {
        $field = $this->fieldReader->get($id);
        if (!$field) {
            throw new FieldNotFoundException();
        }

        $field->name = $name;
        $field->sortId = $sortId;
        $field->parent = $this->resolveParent($parentId);

        $this->fieldWriter->update($field);

        return $field;
    }

    /**
     * @throws FieldNotFoundException
     */
    public function delete(string $id): void {
        $field = $this->fieldReader->get($id);
        if (!$field) {
            throw new FieldNotFoundException();
        }

        $this->stationFieldWriter->unassignAllForField($id);
        $this->fieldWriter->softDelete($field);
    }

    /**
     * @throws FieldNotFoundException
     */
    private function resolveParent(?string $parentId): ?Field {
        if ($parentId === null) {
            return null;
        }

        $parent = $this->fieldReader->get($parentId);
        if (!$parent) {
            throw new FieldNotFoundException();
        }
        return $parent;
    }
}
