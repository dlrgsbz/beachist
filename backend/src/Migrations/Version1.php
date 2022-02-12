<?php

declare(strict_types=1);

namespace DoctrineMigrations;

use Doctrine\DBAL\Schema\Schema;
use Doctrine\Migrations\AbstractMigration;

/**
 * Auto-generated Migration: Please modify to your needs!
 */
final class Version1 extends AbstractMigration
{
    public function getDescription() : string
    {
        return '';
    }

    public function up(Schema $schema) : void
    {
        // this up() migration is auto-generated, please modify it to your needs
        $this->abortIf($this->connection->getDatabasePlatform()->getName() !== 'mysql', 'Migration can only be executed safely on \'mysql\'.');

        $this->addSql('CREATE TABLE entry (id CHAR(36) NOT NULL COMMENT \'(DC2Type:uuid)\', field_id CHAR(36) NOT NULL COMMENT \'(DC2Type:uuid)\', station_id CHAR(36) NOT NULL COMMENT \'(DC2Type:uuid)\', state TINYINT(1) NOT NULL, state_kind VARCHAR(255) DEFAULT NULL, amount INT DEFAULT NULL, note VARCHAR(255) DEFAULT NULL, crew VARCHAR(255) DEFAULT NULL, date DATETIME NOT NULL, PRIMARY KEY(id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB');
        $this->addSql('CREATE TABLE event (id CHAR(36) NOT NULL COMMENT \'(DC2Type:uuid)\', station_id CHAR(36) NOT NULL COMMENT \'(DC2Type:uuid)\', type VARCHAR(255) DEFAULT NULL, date DATETIME NOT NULL, PRIMARY KEY(id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB');
        $this->addSql('CREATE TABLE field (id CHAR(36) NOT NULL COMMENT \'(DC2Type:uuid)\', parent_id CHAR(36) DEFAULT NULL COMMENT \'(DC2Type:uuid)\', name VARCHAR(255) NOT NULL, sort_id INT DEFAULT NULL, PRIMARY KEY(id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB');
        $this->addSql('CREATE TABLE station (id CHAR(36) NOT NULL COMMENT \'(DC2Type:uuid)\', name VARCHAR(255) NOT NULL, sort_id INT DEFAULT NULL, PRIMARY KEY(id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB');
        $this->addSql('CREATE TABLE station_field (id CHAR(36) NOT NULL COMMENT \'(DC2Type:uuid)\', station_id CHAR(36) DEFAULT NULL COMMENT \'(DC2Type:uuid)\', field_id CHAR(36) NOT NULL COMMENT \'(DC2Type:uuid)\', required INT DEFAULT NULL, note VARCHAR(255) DEFAULT NULL, PRIMARY KEY(id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB');
    }

    public function down(Schema $schema) : void
    {
        // this down() migration is auto-generated, please modify it to your needs
        $this->abortIf($this->connection->getDatabasePlatform()->getName() !== 'mysql', 'Migration can only be executed safely on \'mysql\'.');

        $this->addSql('DROP TABLE entry');
        $this->addSql('DROP TABLE event');
        $this->addSql('DROP TABLE field');
        $this->addSql('DROP TABLE station');
        $this->addSql('DROP TABLE station_field');
    }
}
