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

/**
 * @ORM\Entity(repositoryClass="App\Repository\SpecialEventRepository")
 */
class SpecialEvent implements JsonSerializable {
    /**
     * @ORM\Id
     * @ORM\Column(type="uuid", unique=true)
     * @ORM\GeneratedValue(strategy="CUSTOM")
     * @ORM\CustomIdGenerator(class=UuidGenerator::class)
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
    public string $note;

    /**
     * @ORM\Column(type="datetime", nullable=false)
     */
    public DateTimeInterface $date;

    public function __construct(Station $station, string $note, DateTimeInterface $date = null) {
        $this->id = Uuid::uuid4();
        $this->station = $station;
        $this->note = $note;
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
            'note' => $this->note,
            'date' => $this->date->format('c'),
        ];
    }
}
