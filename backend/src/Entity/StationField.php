<?php
declare(strict_types=1);

namespace App\Entity;

use Doctrine\ORM\Mapping as ORM;
use JsonSerializable;
use Ramsey\Uuid\Doctrine\UuidGenerator;

/**
 * @ORM\Entity(repositoryClass="App\Repository\StationFieldRepository")
 */
class StationField implements JsonSerializable {
    /**
     * @ORM\Id
     * @ORM\Column(type="uuid", unique=true)
     * @ORM\GeneratedValue(strategy="CUSTOM")
     * @ORM\CustomIdGenerator(class=UuidGenerator::class)
     */
    public string $id;

    /**
     * @ORM\ManyToOne(targetEntity="Station")
     * @ORM\JoinColumn(name="station_id", referencedColumnName="id", nullable=true)
     */
    public ?Station $station;

    /**
     * @ORM\ManyToOne(targetEntity="Field")
     * @ORM\JoinColumn(name="field_id", referencedColumnName="id", nullable=false)
     */
    public Field $field;

    /**
     * @ORM\Column(type="integer", nullable=true)
     */
    public ?int $required;

    /**
     * @ORM\Column(type="string", nullable=true)
     */
    public ?string $note;

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
            'id' => $this->field->id,
            'internalId' => $this->id,
            'name' => $this->field->name,
            'parent' => $this->field->parent ? $this->field->parent->id : null,
            'sortId' => $this->field->sortId,
            'required' => $this->required,
            'note' => $this->note,
        ];
    }
}
