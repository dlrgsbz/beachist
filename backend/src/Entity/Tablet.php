<?php
declare(strict_types=1);


namespace App\Entity;


use JsonSerializable;
use Ramsey\Uuid\Uuid;
use Ramsey\Uuid\UuidInterface;

/**
 * @ORM\Entity(repositoryClass="App\Repository\TabletRepository")
 */
class Tablet implements JsonSerializable {
    /**
     * @ORM\Id
     * @ORM\Column(type="uuid", unique=true)
     * @ORM\GeneratedValue(strategy="CUSTOM")
     * @ORM\CustomIdGenerator(class=UuidGenerator::class)
     */
    public UuidInterface $id;
    /**
     * @ORM\Column(type="string")
     */
    public string $name;

    public function __construct(string $id, string $name) {
        $this->id = Uuid::fromString($id);
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
        ];
    }
}
