<?php
declare(strict_types=1);

namespace App\Types;

use Doctrine\DBAL\Platforms\AbstractPlatform;
use Doctrine\DBAL\Types\Type;

class SpecialEventType extends Type {
    const SpecialEventType = "specialEventType";

    public function getSQLDeclaration(array $fieldDeclaration, AbstractPlatform $platform) {
        return $platform->getVarcharTypeDeclarationSQL($fieldDeclaration);
    }

    public function convertToPHPValue($value, AbstractPlatform $platform) {
        if ($value === null) {
            return null;
        }

        return \App\Enum\SpecialEventType::make($value);
    }

    public function convertToDatabaseValue($value, AbstractPlatform $platform) {
        if ($value === null) {
            return null;
        }

        return $value->getValue();
    }

    public function getName() {
        return self::SpecialEventType; // modify to match your constant name
    }
}
