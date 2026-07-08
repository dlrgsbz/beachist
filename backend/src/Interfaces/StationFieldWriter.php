<?php
declare(strict_types=1);

namespace App\Interfaces;


use App\Entity\StationField;

interface StationFieldWriter {
    /**
     * Assigns a field to a station. A null $stationId assigns the field to all
     * stations (a single global assignment row). Optionally stores a required
     * amount and a note that are shown in the app.
     */
    function assign(string $fieldId, ?string $stationId, ?int $required = null, ?string $note = null): StationField;

    /**
     * Removes the assignment of a field to a station (or the global assignment
     * when $stationId is null).
     */
    function unassign(string $fieldId, ?string $stationId): void;

    /**
     * Removes every assignment (station-specific and global) of a field.
     */
    function unassignAllForField(string $fieldId): void;
}
