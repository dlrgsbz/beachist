<?php
declare(strict_types=1);

namespace App\Service;


use App\Entity\Field;
use App\Interfaces\FieldReader;
use App\Interfaces\FieldWriter;

class FieldService {
    private FieldReader $fieldReader;
    private FieldWriter $fieldWriter;

    public function __construct(FieldReader $fieldReader, FieldWriter $fieldWriter) {
        $this->fieldReader = $fieldReader;
        $this->fieldWriter = $fieldWriter;
    }

    /**
     * @return Field[]
     */
    public function getAll(): array {
        return $this->fieldReader->getAll();
    }

    public function get(string $id): ?Field {
        return $this->fieldReader->get($id);
    }
}
