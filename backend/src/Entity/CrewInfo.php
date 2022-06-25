<?php
declare(strict_types=1);

namespace App\Entity;

use DateTimeInterface;
use JsonSerializable;
use Doctrine\ORM\Mapping as ORM;

const CREW_ENTITY_DATE_FORMAT = 'Y-m-d';

/**
 * @ORM\Entity(repositoryClass="App\Repository\CrewRepository")
 */
class CrewInfo implements JsonSerializable {
    /**
     * @ORM\Id
     * @ORM\ManyToOne(targetEntity="Station")
     * @ORM\JoinColumn(name="station_id", referencedColumnName="id", nullable=false)
     */
    public Station $station;

    /**
     * @ORM\Column(type="string", nullable=false)
     */
    public string $crew;

    /**
     * @ORM\Id
     * @ORM\Column(type="string", nullable=false)
     */
    public string $date;

    public function __construct(
        Station           $station,
        string            $crew,
        DateTimeInterface $date
    ) {
        $this->station = $station;
        $this->crew = $crew;
        $this->date = $date->format(CREW_ENTITY_DATE_FORMAT);
    }

    public function jsonSerialize(): array {
        return [
            'station' => $this->station->id,
            'crew' => $this->crew,
            'date' => \DateTime::createFromFormat(CREW_ENTITY_DATE_FORMAT, $this->date)->format(CREW_ENTITY_DATE_FORMAT),
        ];
    }
}