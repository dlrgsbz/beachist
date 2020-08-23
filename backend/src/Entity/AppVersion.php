<?php
declare(strict_types=1);


namespace App\Entity;

use DateTime;
use DateTimeInterface;
use Doctrine\ORM\Mapping as ORM;

/**
 * @ORM\Entity(repositoryClass="App\Repository\VersionRepository")
 */
class AppVersion {
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
     * @ORM\Column(type="datetime", nullable=false)
     */
    public DateTimeInterface $date;

    /**
     * @ORM\Column(type="string", nullable=false)
     */
    public string $version;

    public function __construct(
        Station $station,
        string $version,
        DateTimeInterface $date = null
    ) {
        $this->station = $station;
        $this->version = $version;
        if ($date === null) {
            $date = new DateTime();
        }
        $this->date = $date;
    }

    public function getId(): int {
        return $this->id;
    }
}
