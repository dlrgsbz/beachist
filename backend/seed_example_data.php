<?php
declare(strict_types=1);

use App\Entity\Field;
use App\Entity\Station;
use App\Entity\StationField;
use App\Entity\User;
use App\Kernel;
use Ramsey\Uuid\Uuid;

require __DIR__ . '/vendor/autoload.php';

(new Symfony\Component\Dotenv\Dotenv())->bootEnv(__DIR__ . '/.env');

$kernel = new Kernel($_SERVER['APP_ENV'] ?? 'dev', (bool)($_SERVER['APP_DEBUG'] ?? true));
$kernel->boot();
$em = $kernel->getContainer()->get('doctrine')->getManager();

function makeUser(string $name, string $plainPassword, array $roles, string $description): User {
    $u = new User();
    $u->name = $name;
    $u->roles = $roles;
    $u->password = password_hash($plainPassword, PASSWORD_BCRYPT);
    $u->description = $description;
    return $u;
}

// --- Users ---
$userRepo = $em->getRepository(User::class);
if (!$userRepo->findOneBy(['name' => 'admin'])) {
    $em->persist(makeUser('admin', 'admin', ['ROLE_ADMIN'], 'Administrator'));
}
if (!$userRepo->findOneBy(['name' => 'wachhabender'])) {
    $em->persist(makeUser('wachhabender', 'wachhabender', ['ROLE_USER'], 'Wachhabender'));
}
$em->flush();

// Only seed domain data once (skip if stations already present)
$existingStations = $em->getRepository(Station::class)->findAll();
if (count($existingStations) === 0) {
    // --- Stations ---
    $stationDefs = ['Turm Nord', 'Turm Mitte', 'Turm Süd'];
    $stations = [];
    foreach ($stationDefs as $i => $name) {
        $s = new Station(Uuid::uuid4()->toString(), $name);
        $s->sortId = $i + 1;
        $em->persist($s);
        $stations[] = $s;
    }
    $em->flush();

    // --- Fields (items), with heading parents ---
    // headings
    $headings = [];
    foreach (['Erste Hilfe', 'Ausrüstung', 'Wetter'] as $i => $name) {
        $h = new Field(Uuid::uuid4()->toString(), $name);
        $h->sortId = ($i + 1) * 100;
        $h->parent = null;
        $em->persist($h);
        $headings[$name] = $h;
    }
    $em->flush();

    // children: name => [heading, sortId]
    $childDefs = [
        ['Verbandskasten vollständig', 'Erste Hilfe', 110],
        ['Beatmungsbeutel geprüft', 'Erste Hilfe', 120],
        ['Defibrillator einsatzbereit', 'Erste Hilfe', 130],
        ['Rettungsboard vorhanden', 'Ausrüstung', 210],
        ['Fernglas vorhanden', 'Ausrüstung', 220],
        ['Funkgerät geladen', 'Ausrüstung', 230],
        ['Windrichtung notiert', 'Wetter', 310],
        ['Wassertemperatur gemessen', 'Wetter', 320],
    ];
    $fields = [];
    foreach ($childDefs as [$name, $headingName, $sortId]) {
        $f = new Field(Uuid::uuid4()->toString(), $name);
        $f->sortId = $sortId;
        $f->parent = $headings[$headingName];
        $em->persist($f);
        $fields[$name] = $f;
    }
    $em->flush();

    // --- Assignments ---
    // helper
    $assign = function (Field $field, ?Station $station, ?int $required = null, ?string $note = null) use ($em) {
        $sf = new StationField();
        $sf->field = $field;
        $sf->station = $station;
        $sf->required = $required;
        $sf->note = $note;
        $em->persist($sf);
    };

    // Global assignments (all stations): first-aid basics
    $assign($fields['Verbandskasten vollständig'], null, 1);
    $assign($fields['Beatmungsbeutel geprüft'], null, 1);
    $assign($fields['Funkgerät geladen'], null, 1);
    $assign($fields['Windrichtung notiert'], null);

    // Per-station assignments
    $assign($fields['Defibrillator einsatzbereit'], $stations[0], 1, 'Nur Turm Nord hat einen AED');
    $assign($fields['Rettungsboard vorhanden'], $stations[0], 2);
    $assign($fields['Rettungsboard vorhanden'], $stations[1], 1);
    $assign($fields['Fernglas vorhanden'], $stations[1]);
    $assign($fields['Wassertemperatur gemessen'], $stations[2], 1, 'Messung um 10 und 16 Uhr');

    $em->flush();

    echo "Seeded: " . count($stations) . " stations, " . (count($headings) + count($fields)) . " fields.\n";
} else {
    echo "Domain data already present, skipped station/field seeding.\n";
}

echo "Users ready: admin/admin (ROLE_ADMIN), wachhabender/wachhabender (ROLE_USER)\n";
echo "Done.\n";
