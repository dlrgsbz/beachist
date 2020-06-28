<?php
declare(strict_types=1);

namespace App\Types;

use Doctrine\DBAL\Platforms\AbstractPlatform;
use Doctrine\DBAL\Types\Type;

class StateKind extends Type {
    const StateKind = "statekind";

    public function getSQLDeclaration(array $fieldDeclaration, AbstractPlatform $platform) {
        return $platform->getVarcharTypeDeclarationSQL($fieldDeclaration);
    }

    public function convertToPHPValue($value, AbstractPlatform $platform) {
        if ($value === null) {
            return null;
        }

        return \App\Entity\StateKind::make($value);
    }

    public function convertToDatabaseValue($value, AbstractPlatform $platform) {
        if ($value === null) {
            return null;
        }

        return $value->getValue();
    }

    public function getName() {
        return self::StateKind; // modify to match your constant name
    }
}
