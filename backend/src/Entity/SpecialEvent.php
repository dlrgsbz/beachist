<?php
declare(strict_types=1);


namespace App\Entity;

use App\Enum\SpecialEventType;
use DateTime;
use DateTimeInterface;
use Doctrine\ORM\Mapping as ORM;
use JsonSerializable;
use Ramsey\Uuid\Doctrine\UuidGenerator;
use Ramsey\Uuid\Uuid;
use Ramsey\Uuid\UuidInterface;
use Spatie\Enum\Enum;

/**
 * @ORM\Entity(repositoryClass="App\Repository\SpecialEventRepository")
 */
class SpecialEvent implements JsonSerializable {
    /**
     * @ORM\Id
     * @ORM\Column(type="uuid", unique=true)
     * @ORM\GeneratedValue(strategy="NONE")
     */
    public UuidInterface $id;

    /**
     * @ORM\ManyToOne(targetEntity="Station")
     * @ORM\JoinColumn(name="station_id", referencedColumnName="id", nullable=false)
     */
    public Station $station;

    /**
     * @ORM\Column(type="string")
     */
    public string $title;

    /**
     * @ORM\Column(type="string")
     */
    public string $note;

    /**
     * @ORM\Column(type="string")
     */
    public string $notifier;

    /**
     * @ORM\Column(type="specialEventType", nullable=true)
     */
    public SpecialEventType $type;

    /**
     * @ORM\Column(type="datetime", nullable=false)
     */
    public DateTimeInterface $date;

    public function __construct(Station $station, string $title, string $note, string $notifier, SpecialEventType $type, DateTimeInterface $date = null, UuidInterface $id = null) {
        if (!$id) {
            $id = Uuid::uuid4();
        }
        $this->id = $id;
        $this->title = $title;
        $this->station = $station;
        $this->note = $note;
        $this->type = $type;
        $this->notifier = $notifier;
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
            'id' => $this->id->toString(),
            'station' => $this->station->id,
            'title' => $this->title,
            'note' => $this->note,
            'notifier' => $this->notifier,
            'type' => $this->type->getValue(),
            'date' => $this->date->format('c'),
        ];
    }
}
