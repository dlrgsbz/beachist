<?php
declare(strict_types=1);


namespace App\Entity;


use JsonSerializable;

class DailyStats implements JsonSerializable {
    public \DateTimeInterface $date;
    public int $firstAid;
    public int $search;

    public static function empty(string $date): DailyStats {
        return new static(['date' => $date, 'firstAid' => 0, 'search' => 0]);
    }

    public function __construct(array $properties = []) {
        foreach ($properties as $key => $value) {
            if ($key === 'firstAid' || $key === 'search') {
                $value = (int)$value;
            } else {
                $value = \DateTime::createFromFormat('Y-m-d', $value);
            }
            $this->{$key} = $value;
        }
    }

    public function jsonSerialize() {
        return [
            'date' => $this->date->format('Y-m-d'),
            'firstAid' => $this->firstAid,
            'search' => $this->search,
        ];
    }
}
