<?php
declare(strict_types=1);


namespace App\Entity;

use DateTimeInterface;
use Doctrine\ORM\Mapping as ORM;
use Spatie\Enum\Enum;

/**
 * @ORM\Entity(repositoryClass="App\Repository\EventRepository")
 */
class Event {
    /**
     * @var integer
     *
     * @ORM\Id
     * @ORM\Column(type="integer")
     * @ORM\GeneratedValue
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

    public function __construct(Station $station, EventType $type, DateTimeInterface $date) {
        $this->station = $station;
        $this->type = $type;
        $this->date = $date;
    }


    /**
     * @return int
     */
    public function getId(): int {
        return $this->id;
    }
}

/**
 * @method static self firstAid()
 * @method static self search()
 */
class EventType extends Enum {
}
