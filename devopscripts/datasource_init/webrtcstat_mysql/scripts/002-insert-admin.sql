INSERT INTO `ObserveRTC`.`Users`
(`id`,
 `uuid`,
 `username`,
 `password_digest`,
 `password_salt`,
 `role`)
VALUES (1,
        UNHEX(REPLACE('58a6314b-188c-4659-a046-553a7f8c96de', '-', '')),
        'balazs',
        UNHEX('e77183b020e803e858c39b95652c81084f19ed11e2e2d18433bcb2c8a8a46768'),
        UNHEX('e12'),
        'administrator');

INSERT INTO `ObserveRTC`.`Observers`
(`id`,
 `uuid`,
 `name`,
 `description`)
VALUES (1,
        UNHEX(REPLACE('86ed98c6-b001-48bb-b31e-da638b979c72', '-', '')),
        'demo',
        'demo description');


INSERT INTO `ObserveRTC`.`Organisations`
(`id`,
 `uuid`,
 `name`,
 `description`)
VALUES (1,
        UNHEX(REPLACE('86ed98c6-b001-48bb-b31e-da638b979c72', '-', '')),
        'MyOrganisation',
        'Calculates the median for the DemoSamples');

INSERT INTO `ObserveRTC`.`ObserverOrganisations`
(`observer_id`,
 `organisation_id`)
VALUES (1,
        1);

