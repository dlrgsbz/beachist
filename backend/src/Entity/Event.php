<?php
declare(strict_types=1);


namespace App\Entity;

use DateTime;
use DateTimeInterface;
use Doctrine\ORM\Mapping as ORM;
use Ramsey\Uuid\Uuid;

/**
 * @ORM\Entity(repositoryClass="App\Repository\EventRepository")
 */
class Event {
    /**
     * @var integer
     *
     * @ORM\Id
     * @ORM\Column(type="uuid", unique=true)
     */
    private $id;

    /**
     * @ORM\ManyToOne(targetEntity="Station")
     * @ORM\JoinColumn(name="station_id", referencedColumnName="id", nullable=false)
     */
    public Station $station;

    /**
     * @ORM\Column(type="eventType", nullable=true)
     */
    public EventType $type;

    /**
     * @ORM\Column(type="datetime")
     */
    public DateTimeInterface $date;

    public function __construct(Station $station, EventType $type, DateTimeInterface $date = null, string $id = null) {
        $this->station = $station;
        $this->type = $type;
        if ($date === null) {
            $date = new DateTime();
        }
        $this->date = $date;
        if ($id === null) {
            $id = Uuid::uuid1()->toString();
        }
        $this->id = $id;
    }


    /**
     * @return string
     */
    public function getId(): string {
        return $this->id;
    }
}
