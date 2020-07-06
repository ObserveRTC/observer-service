# 'Query for CrudRepositoryTest.shouldSaveANotExistingEntity()'
# ---------------------------------------------------------
insert into `wobserver`.`Observers` (`uuid`, `name`, `description`) values (X'f02f081248f544dbaddd8acfd7bbcfa0', 'name1', 'desc1') on duplicate key update `wobserver`.`Observers`.`uuid` = X'f02f081248f544dbaddd8acfd7bbcfa0', `wobserver`.`Observers`.`name` = 'name1', `wobserver`.`Observers`.`description` = 'desc1';
@ rows: 1

# 'Query for CrudRepositoryTest.shouldSaveAnExistingEntity()'
# ---------------------------------------------------------
insert into `wobserver`.`Observers` (`uuid`, `name`, `description`) values (X'e02f081248f544dbaddd8acfd7bbcfa0', 'name1', 'desc1') on duplicate key update `wobserver`.`Observers`.`uuid` = X'e02f081248f544dbaddd8acfd7bbcfa0', `wobserver`.`Observers`.`name` = 'name1', `wobserver`.`Observers`.`description` = 'desc1';
@ rows: 1

# 'Query for CrudRepositoryTest.shouldNotUpdateANotExistingEntity()'
# ---------------------------------------------------------
update `wobserver`.`observers` set `wobserver`.`observers`.`name` = 'name1', `wobserver`.`observers`.`description` = 'desc1' where `wobserver`.`observers`.`uuid` = X'f02f081248f544dbaddd8acfd7bbcfa0';
@ rows: 1

# 'Query for CrudRepositoryTest.shouldUpdateAnExistingEntity()'
# ---------------------------------------------------------
update `wobserver`.`Observers` set `wobserver`.`Observers`.`name` = 'name1', `wobserver`.`Observers`.`description` = 'desc1' where `wobserver`.`Observers`.`uuid` = X'e02f081248f544dbaddd8acfd7bbcfa0';
@ rows: 1

# 'Query for CrudRepositoryTest.shouldSaveAllExistingEntities()'
# ---------------------------------------------------------
 
# 'Query for CrudRepositoryTest.shouldSaveAllNotExistingEntities()'
# ---------------------------------------------------------
 
# 'Query for CrudRepositoryTest.shouldFindByIDAnExistingEntity()'
# ---------------------------------------------------------
select `wobserver`.`Observers`.`uuid`, `wobserver`.`Observers`.`name`, `wobserver`.`Observers`.`description` from `wobserver`.`Observers` where `wobserver`.`Observers`.`uuid` = X'e02f081248f544dbaddd8acfd7bbcfa0';
> uuid name description
> ------------------------------------ ----- ------------
> 123456789012345678901234567890123456 name1 description1
@ rows: 1

# 'Query for CrudRepositoryTest.shouldExistsByIDForExistingEntity()'
# ---------------------------------------------------------
select 1 as `one` from dual where exists (select 1 as `one` from `wobserver`.`Observers` where `wobserver`.`Observers`.`uuid` = X'e02f081248f544dbaddd8acfd7bbcfa0');
@ rows: 1

# 'Query for CrudRepositoryTest.shouldFindAllI()'
# ---------------------------------------------------------
select `wobserver`.`Observers`.`uuid`, `wobserver`.`Observers`.`name`, `wobserver`.`Observers`.`description` from `wobserver`.`Observers` order by `wobserver`.`Observers`.`id` limit ?;
> uuid `name` description
> ------------------------------------ ----- ------------
> 123456789012345678901234567890123456 name1 description1
> 123456789012345678901234567890123457 name2 description2
@ rows: 2

# 'Query for CrudRepositoryTest.shouldCount()'
# ---------------------------------------------------------
select count(*) from `wobserver`.`Observers`;
@ rows: 2
 
# 'Query for CrudRepositoryTest.shouldNotDeleteByIdANotExistingEntity()'
# ---------------------------------------------------------
delete from `wobserver`.`Observers` where `wobserver`.`Observers`.`uuid` in (X'f02f081248f544dbaddd8acfd7bbcfa0', X'f02f081248f544dbaddd8acfd7bbcfa9');
@ exception: thrown
 
# 'Query for CrudRepositoryTest.shouldDeleteByIdAnExistingEntity()'
# ---------------------------------------------------------
delete from `wobserver`.`Observers` where `wobserver`.`Observers`.`uuid` in (X'e02f081248f544dbaddd8acfd7bbcfa0', X'e02f081248f544dbaddd8acfd7bbcfa9');
@ rows: 2

# 'Query for CrudRepositoryTest.shouldDeleteAnExistingEntity()'
# ---------------------------------------------------------
delete from `wobserver`.`Observers` where `wobserver`.`Observers`.`uuid` in (X'e02f081248f544dbaddd8acfd7bbcfa0');
@ rows: 2

# 'Query for CrudRepositoryTest.shouldDeleteAllExistingEntities()'
# --------------------------------------------------------- 
delete from `wobserver`.`Observers` where `wobserver`.`Observers`.`uuid` in (X'e02f081248f544dbaddd8acfd7bbcfa0', X'e02f081248f544dbaddd8acfd7bbcfa9');
@ rows: 2

# 'Query for CrudRepositoryTest.shouldPurgeAllExistingEntities'
# ---------------------------------------------------------
delete from `wobserver`.`Observers`; 