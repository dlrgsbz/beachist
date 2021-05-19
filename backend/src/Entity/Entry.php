<?php
declare(strict_types=1);


namespace App\Entity;

use DateTime;
use DateTimeInterface;
use Doctrine\ORM\Mapping as ORM;
use JsonSerializable;
use Ramsey\Uuid\Doctrine\UuidGenerator;
use Ramsey\Uuid\Uuid;
use Ramsey\Uuid\UuidInterface;
use Spatie\Enum\Enum;

/**
 * @ORM\Entity(repositoryClass="App\Repository\EntryRepository")
 */
class Entry implements JsonSerializable {
    /**
     * @ORM\Id
     * @ORM\Column(type="uuid", unique=true)
     * @ORM\GeneratedValue(strategy="CUSTOM")
     * @ORM\CustomIdGenerator(class=UuidGenerator::class)
     */
    public UuidInterface $id;

    /**
     * @ORM\ManyToOne(targetEntity="Field")
     * @ORM\JoinColumn(name="field_id", referencedColumnName="id", nullable=false)
     */
    public Field $field;

    /**
     * @ORM\ManyToOne(targetEntity="Station")
     * @ORM\JoinColumn(name="station_id", referencedColumnName="id", nullable=false)
     */
    public Station $station;

    /**
     * @ORM\Column(type="boolean")
     */
    public bool $state;

    /**
     * @ORM\Column(type="statekind", nullable=true)
     */
    public ?StateKind $stateKind;

    /**
     * @ORM\Column(type="integer", nullable=true)
     */
    public ?int $amount;

    /**
     * @ORM\Column(type="string", nullable=true)
     */
    public ?string $note;

    /**
     * @ORM\Column(type="string", nullable=true)
     */
    public ?string $crew;

    /**
     * @ORM\Column(type="datetime", nullable=false)
     */
    public DateTimeInterface $date;

    public function __construct(
        Field $field,
        Station $station,
        bool $state,
        StateKind $stateKind = null,
        DateTimeInterface $date = null,
        int $amount = null,
        string $note = null,
        string $crew = null
    ) {
        $this->id = Uuid::uuid4();
        $this->field = $field;
        $this->station = $station;
        $this->state = $state;
        $this->stateKind = $stateKind;
        $this->amount = $amount;
        $this->note = $note;
        $this->crew = $crew;
        if (!$date) {
            $date = new DateTime();
        }
        $this->date = $date;
    }

    /**
     * Specify data which should be serialized to JSON
     *
     * @link https://php.net/manual/en/jsonserializable.jsonserialize.php
     * @return mixed data which can be serialized by <b>json_encode</b>,
     * which is a value of any type other than a resource.
     * @since 5.4.0
     */
    public function jsonSerialize() {
        return [
            'id' => $this->id,
            'field' => $this->field->id,
            'station' => $this->station->id,
            'state' => $this->state,
            'stateKind' => $this->stateKind ? $this->stateKind->getValue() : null,
            'amount' => $this->amount,
            'note' => $this->note,
            'crew' => $this->crew,
            'date' => $this->date->format('c'),
        ];
    }
}
