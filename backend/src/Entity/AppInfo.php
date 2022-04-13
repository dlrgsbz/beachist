<?php
declare(strict_types=1);


namespace App\Entity;

use DateTime;
use DateTimeInterface;
use Doctrine\ORM\Mapping as ORM;

/**
 * @ORM\Entity(repositoryClass="App\Repository\AppInfoRepository")
 */
class AppInfo implements \JsonSerializable {
    /**
     * @var integer
     *
     * @ORM\Id
     * @ORM\Column(type="integer")
     * @ORM\GeneratedValue
     */
    public int $id;

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

    /**
     * @ORM\Column(type="integer", nullable=true)
     */
    public ?int $versionCode = null;

    /**
     * @ORM\Column(type="boolean", nullable=false)
     */
    public bool $online = false;

    public function __construct(
        Station $station,
        string $version,
        int $versionCode = null,
        bool $online = false,
        DateTimeInterface $date = null
    ) {
        $this->station = $station;
        $this->version = $version;
        if ($date === null) {
            $date = new DateTime();
        }
        $this->date = $date;
        $this->online = $online;
        $this->versionCode = $versionCode;
    }

    public function jsonSerialize(): array {
        return [
            'station' => $this->station->id,
            'version' => $this->version,
            'versionCode' => $this->versionCode,
            'online' => $this->online,
            'date' => $this->date->format('c'),
        ];
    }
}
