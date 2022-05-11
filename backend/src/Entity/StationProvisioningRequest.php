<?php
declare(strict_types=1);


namespace App\Entity;

use DateInterval;
use DateTime;
use DateTimeInterface;
use Doctrine\ORM\Mapping as ORM;
use JsonSerializable;
use Ramsey\Uuid\Doctrine\UuidGenerator;
use Ramsey\Uuid\Uuid;
use Ramsey\Uuid\UuidInterface;
use Spatie\Enum\Enum;

/**
 * @ORM\Entity(repositoryClass="App\Repository\ProvisioningRepository")
 */
class StationProvisioningRequest implements JsonSerializable {
    /**
     * @ORM\Id
     * @ORM\GeneratedValue
     * @ORM\Column(type="integer")
     */
    public int $id;

    /**
     * @ORM\ManyToOne(targetEntity="Station")
     * @ORM\JoinColumn(name="station_id", referencedColumnName="id", nullable=false)
     */
    public Station $station;

    /**
     * @ORM\Column(type="string", nullable=false)
     */
    public string $password;

    /**
     * @ORM\Column(type="datetime", nullable=false)
     */
    public DateTimeInterface $expiresAt;

    /**
     * @ORM\Column(type="boolean", nullable=false)
     */
    public bool $active = true;

    public function __construct(
        Station $station,
        string $password,
        DateTimeInterface $expiresAt = null
    ) {
        $this->id = 0;
        $this->station = $station;
        $this->password = $password;
        if (!$expiresAt) {
            $expiresAt = new DateTime();
            $expiresAt->add(new DateInterval('P1D'));
        }
        $this->expiresAt = $expiresAt;
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
            'station' => $this->station->id,
            'password' => $this->password,
            'expiresAt' => $this->expiresAt->format('c'),
        ];
    }
}
