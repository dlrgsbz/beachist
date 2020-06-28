<?php
declare(strict_types=1);

namespace App\Entity;

use Doctrine\ORM\Mapping as ORM;
use JsonSerializable;
use Ramsey\Uuid\Doctrine\UuidGenerator;

/**
 * @ORM\Entity(repositoryClass="App\Repository\StationRepository")
 */
class Field implements JsonSerializable {
    /**
     * @ORM\Id
     * @ORM\Column(type="uuid", unique=true)
     * @ORM\GeneratedValue(strategy="CUSTOM")
     * @ORM\CustomIdGenerator(class=UuidGenerator::class)
     */
    public string $id;

    /**
     * @ORM\Column(type="string")
     */
    public string $name;

    /**
     * @ORM\Column(type="integer", nullable=true)
     */
    public ?int $sortId;

    /**
     * @ORM\ManyToOne(targetEntity="Field")
     * @ORM\JoinColumn(name="parent_id", referencedColumnName="id", nullable=true)
     */
    public ?Field $parent;

    /**
     * Station constructor.
     *
     * @param string $id
     * @param string $name
     */
    public function __construct(string $id, string $name) {
        $this->id = $id;
        $this->name = $name;
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
            'name' => $this->name,
            'parent' => $this->parent ? $this->parent->id : null,
        ];
    }
}
