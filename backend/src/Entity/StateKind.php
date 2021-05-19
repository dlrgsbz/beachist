<?php
declare(strict_types=1);
namespace App\Entity;

use Spatie\Enum\Enum;

/**
 * @method static self broken()
 * @method static self tooLittle()
 * @method static self other()
 */
final class StateKind extends Enum {
}
