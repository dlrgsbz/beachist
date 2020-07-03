<?php
declare(strict_types=1);


namespace App\Service;


use App\Entity\Tablet;
use App\Interfaces\TabletReader;
use App\Interfaces\TabletWriter;
use Exception;

class TabletService {
    private TabletWriter $tabletWriter;
    private TabletReader $tabletReader;

    public function __construct(TabletWriter $tabletWriter, TabletReader $tabletReader) {
        $this->tabletWriter = $tabletWriter;
        $this->tabletReader = $tabletReader;
    }

    public function getTablets() {
        return $this->tabletReader->getTablets();
    }

    public function getTablet(string $id): ?Tablet {
        return $this->tabletReader->getTablet($id);
    }

    /**
     * @throws TabletAlreadyExistsException
     */
    public function create(string $id, string $name): string {
        $existing = $this->getTablet($id);
        if ($existing !== null) {
            throw new TabletAlreadyExistsException();
        }

        $tablet = new Tablet($id, $name);

        return $this->tabletWriter->createTablet($tablet);
    }
}

class TabletAlreadyExistsException extends Exception {}
