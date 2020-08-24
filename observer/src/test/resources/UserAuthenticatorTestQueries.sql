select `wobserver`.`Users`.`password_digest`, `wobserver`.`Users`.`password_salt`, `wobserver`.`Users`.`role` from `wobserver`.`Users` where `wobserver`.`Users`.`username` = 'test@test.test';
> password_digest   password_salt role
> ---------------   ------------- -------------
> 1                 N/A           administrator
@ rows: 1
