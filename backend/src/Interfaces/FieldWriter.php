<?php
declare(strict_types=1);

namespace App\Interfaces;


use App\Entity\Field;

interface FieldWriter {
    /**
     * Persists a new field and returns its id.
     */
    function create(Field $field): string;

    function update(Field $field): void;

    /**
     * Marks the field as deleted without removing it or its history.
     */
    function softDelete(Field $field): void;

    /**
     * Renumbers the sortId of the given fields to match the provided order.
     *
     * @param string[] $orderedIds
     */
    function reorder(array $orderedIds): void;
}
