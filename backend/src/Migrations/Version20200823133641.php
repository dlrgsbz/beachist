<?php

declare(strict_types=1);

namespace DoctrineMigrations;

use Doctrine\DBAL\Schema\Schema;
use Doctrine\Migrations\AbstractMigration;

/**
 * Auto-generated Migration: Please modify to your needs!
 */
final class Version20200823133641 extends AbstractMigration
{
    public function getDescription() : string
    {
        return '';
    }

    public function up(Schema $schema) : void
    {
        // this up() migration is auto-generated, please modify it to your needs
        $this->abortIf($this->connection->getDatabasePlatform()->getName() !== 'mysql', 'Migration can only be executed safely on \'mysql\'.');

        $this->addSql('CREATE TABLE app_version (date DATETIME NOT NULL, station_id CHAR(36) NOT NULL COMMENT \'(DC2Type:uuid)\', version VARCHAR(255) NOT NULL, INDEX IDX_5241538E21BDB235 (station_id), PRIMARY KEY(station_id, date)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB');
        $this->addSql('ALTER TABLE app_version ADD CONSTRAINT FK_5241538E21BDB235 FOREIGN KEY (station_id) REFERENCES station (id)');
        $this->addSql('ALTER TABLE event CHANGE id id INT AUTO_INCREMENT NOT NULL, CHANGE station_id station_id CHAR(36) NOT NULL COMMENT \'(DC2Type:uuid)\', CHANGE type type VARCHAR(255) DEFAULT NULL');
        $this->addSql('ALTER TABLE event ADD CONSTRAINT FK_3BAE0AA721BDB235 FOREIGN KEY (station_id) REFERENCES station (id)');
        $this->addSql('CREATE INDEX IDX_3BAE0AA721BDB235 ON event (station_id)');
        $this->addSql('ALTER TABLE special_event CHANGE note note VARCHAR(255) NOT NULL, CHANGE notifier notifier VARCHAR(255) NOT NULL, CHANGE type type VARCHAR(255) DEFAULT NULL');
        $this->addSql('ALTER TABLE special_event ADD CONSTRAINT FK_9790A4E621BDB235 FOREIGN KEY (station_id) REFERENCES station (id)');
        $this->addSql('ALTER TABLE station_field CHANGE id id CHAR(36) NOT NULL COMMENT \'(DC2Type:uuid)\', CHANGE station_id station_id CHAR(36) DEFAULT NULL COMMENT \'(DC2Type:uuid)\', CHANGE field_id field_id CHAR(36) NOT NULL COMMENT \'(DC2Type:uuid)\', CHANGE note note VARCHAR(255) DEFAULT NULL');
        $this->addSql('ALTER TABLE station_field ADD CONSTRAINT FK_3733A6E21BDB235 FOREIGN KEY (station_id) REFERENCES station (id)');
        $this->addSql('ALTER TABLE station_field ADD CONSTRAINT FK_3733A6E443707B0 FOREIGN KEY (field_id) REFERENCES field (id)');
        $this->addSql('CREATE INDEX IDX_3733A6E21BDB235 ON station_field (station_id)');
        $this->addSql('CREATE INDEX IDX_3733A6E443707B0 ON station_field (field_id)');
        $this->addSql('ALTER TABLE field CHANGE id id CHAR(36) NOT NULL COMMENT \'(DC2Type:uuid)\', CHANGE name name VARCHAR(255) NOT NULL, CHANGE parent_id parent_id CHAR(36) DEFAULT NULL COMMENT \'(DC2Type:uuid)\'');
        $this->addSql('ALTER TABLE field ADD CONSTRAINT FK_5BF54558727ACA70 FOREIGN KEY (parent_id) REFERENCES field (id)');
        $this->addSql('CREATE INDEX IDX_5BF54558727ACA70 ON field (parent_id)');
        $this->addSql('ALTER TABLE entry CHANGE id id CHAR(36) NOT NULL COMMENT \'(DC2Type:uuid)\', CHANGE station_id station_id CHAR(36) NOT NULL COMMENT \'(DC2Type:uuid)\', CHANGE field_id field_id CHAR(36) NOT NULL COMMENT \'(DC2Type:uuid)\', CHANGE state_kind state_kind VARCHAR(255) DEFAULT NULL, CHANGE note note VARCHAR(255) DEFAULT NULL');
        $this->addSql('ALTER TABLE entry ADD CONSTRAINT FK_2B219D70443707B0 FOREIGN KEY (field_id) REFERENCES field (id)');
        $this->addSql('ALTER TABLE entry ADD CONSTRAINT FK_2B219D7021BDB235 FOREIGN KEY (station_id) REFERENCES station (id)');
        $this->addSql('CREATE INDEX IDX_2B219D70443707B0 ON entry (field_id)');
        $this->addSql('CREATE INDEX IDX_2B219D7021BDB235 ON entry (station_id)');
        $this->addSql('ALTER TABLE station CHANGE id id CHAR(36) NOT NULL COMMENT \'(DC2Type:uuid)\', CHANGE name name VARCHAR(255) NOT NULL');
    }

    public function down(Schema $schema) : void
    {
        // this down() migration is auto-generated, please modify it to your needs
        $this->abortIf($this->connection->getDatabasePlatform()->getName() !== 'mysql', 'Migration can only be executed safely on \'mysql\'.');

        $this->addSql('DROP TABLE app_version');
        $this->addSql('ALTER TABLE entry DROP FOREIGN KEY FK_2B219D70443707B0');
        $this->addSql('ALTER TABLE entry DROP FOREIGN KEY FK_2B219D7021BDB235');
        $this->addSql('DROP INDEX IDX_2B219D70443707B0 ON entry');
        $this->addSql('DROP INDEX IDX_2B219D7021BDB235 ON entry');
        $this->addSql('ALTER TABLE entry CHANGE id id CHAR(64) CHARACTER SET utf8 DEFAULT \'\' NOT NULL COLLATE `utf8_unicode_ci`, CHANGE field_id field_id CHAR(64) CHARACTER SET utf8 DEFAULT \'\' NOT NULL COLLATE `utf8_unicode_ci`, CHANGE station_id station_id CHAR(64) CHARACTER SET utf8 NOT NULL COLLATE `utf8_unicode_ci`, CHANGE state_kind state_kind VARCHAR(10) CHARACTER SET utf8 DEFAULT NULL COLLATE `utf8_unicode_ci`, CHANGE note note VARCHAR(256) CHARACTER SET utf8 DEFAULT NULL COLLATE `utf8_unicode_ci`');
        $this->addSql('ALTER TABLE event DROP FOREIGN KEY FK_3BAE0AA721BDB235');
        $this->addSql('DROP INDEX IDX_3BAE0AA721BDB235 ON event');
        $this->addSql('ALTER TABLE event CHANGE id id INT UNSIGNED AUTO_INCREMENT NOT NULL, CHANGE station_id station_id CHAR(64) CHARACTER SET utf8 DEFAULT \'\' NOT NULL COLLATE `utf8_unicode_ci`, CHANGE type type CHAR(10) CHARACTER SET utf8 DEFAULT \'\' NOT NULL COLLATE `utf8_unicode_ci`');
        $this->addSql('ALTER TABLE field DROP FOREIGN KEY FK_5BF54558727ACA70');
        $this->addSql('DROP INDEX IDX_5BF54558727ACA70 ON field');
        $this->addSql('ALTER TABLE field CHANGE id id CHAR(64) CHARACTER SET utf8 DEFAULT \'\' NOT NULL COLLATE `utf8_unicode_ci`, CHANGE parent_id parent_id CHAR(64) CHARACTER SET utf8 DEFAULT NULL COLLATE `utf8_unicode_ci`, CHANGE name name VARCHAR(265) CHARACTER SET utf8 DEFAULT \'\' NOT NULL COLLATE `utf8_unicode_ci`');
        $this->addSql('ALTER TABLE special_event DROP FOREIGN KEY FK_9790A4E621BDB235');
        $this->addSql('ALTER TABLE special_event CHANGE note note TEXT CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_unicode_ci`, CHANGE notifier notifier VARCHAR(64) CHARACTER SET utf8mb4 DEFAULT \'\' NOT NULL COLLATE `utf8mb4_unicode_ci`, CHANGE type type CHAR(6) CHARACTER SET utf8mb4 DEFAULT \'\' NOT NULL COLLATE `utf8mb4_unicode_ci`');
        $this->addSql('ALTER TABLE station CHANGE id id CHAR(64) CHARACTER SET utf8 DEFAULT \'\' NOT NULL COLLATE `utf8_unicode_ci`, CHANGE name name VARCHAR(64) CHARACTER SET utf8 DEFAULT \'\' NOT NULL COLLATE `utf8_unicode_ci`');
        $this->addSql('ALTER TABLE station_field DROP FOREIGN KEY FK_3733A6E21BDB235');
        $this->addSql('ALTER TABLE station_field DROP FOREIGN KEY FK_3733A6E443707B0');
        $this->addSql('DROP INDEX IDX_3733A6E21BDB235 ON station_field');
        $this->addSql('DROP INDEX IDX_3733A6E443707B0 ON station_field');
        $this->addSql('ALTER TABLE station_field CHANGE id id CHAR(64) CHARACTER SET utf8 DEFAULT \'\' NOT NULL COLLATE `utf8_unicode_ci`, CHANGE station_id station_id CHAR(64) CHARACTER SET utf8 DEFAULT \'\' COLLATE `utf8_unicode_ci`, CHANGE field_id field_id CHAR(64) CHARACTER SET utf8 DEFAULT \'\' NOT NULL COLLATE `utf8_unicode_ci`, CHANGE note note TEXT CHARACTER SET utf8 DEFAULT NULL COLLATE `utf8_unicode_ci`');
    }
}
