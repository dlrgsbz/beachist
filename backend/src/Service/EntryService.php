<?php
declare(strict_types=1);


namespace App\Service;


use App\Entity\Entry;
use App\Entity\StateKind;
use App\Interfaces\EntryReader;
use App\Interfaces\EntryWriter;
use App\Interfaces\FieldNotFoundException;
use App\Interfaces\FieldReader;
use App\Interfaces\StationNotFoundException;
use App\Interfaces\StationReader;
use DateTimeInterface;
use JsonSerializable;

class EntryService {
    private EntryWriter $entryWriter;
    private EntryReader $entryReader;
    private FieldReader $fieldReader;
    private StationReader $stationReader;

    public function __construct(
        EntryWriter $entryWriter,
        EntryReader $entryReader,
        FieldReader $fieldReader,
        StationReader $stationReader
    ) {
        $this->entryWriter = $entryWriter;
        $this->entryReader = $entryReader;
        $this->fieldReader = $fieldReader;
        $this->stationReader = $stationReader;
    }

    /**
     * @throws
     */
    public function create(
        string $stationId,
        string $fieldId,
        bool $state,
        StateKind $stateKind = null,
        int $amount = null,
        string $note = null
    ): string {
        $field = $this->fieldReader->get($fieldId);
        if (!$field) {
            throw new FieldNotFoundException();
        }

        $station = $this->stationReader->getStation($stationId);
        if (!$station) {
            throw new StationNotFoundException();
        }

        $entry = new Entry($field, $station, $state, $stateKind, null, $amount, $note);

        return $this->entryWriter->create($entry);
    }

    /**
     * @return JsonSerializable[]
     */
    function get(DateTimeInterface $date): array {
        return $this->entryReader->get($date);
    }

    /** @throws */
    public function getByStation(DateTimeInterface $date, string $stationId) {
        $station = $this->stationReader->getStation($stationId);
        if (!$station) {
            throw new StationNotFoundException();
        }

        return $this->entryReader->getByStation($date, $stationId);
    }
}
