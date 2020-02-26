ALTER SESSION SET CONTAINER = ORCLPDB1;
alter session set current_schema="SAMPLESERVICE_SERVICE";

select * from all_users order by created desc;

select * from SAMPLESERVICE_SERVICE.PLAY_EVOLUTIONS
select * from SAMPLESERVICE_SERVICE."read_side_offsets"
select * from PLAY_EVOLUTIONS
select * from "read_side_offsets"
truncate table SAMPLESERVICE_SERVICE.PLAY_EVOLUTIONS
commit

select * from ALL_TABLES where owner='SAMPLESERVICE_SERVICE'
select owner, table_name, column_name, data_type, data_length, column_id from all_TAB_COLUMNS where owner='SAMPLESERVICE_SERVICE' and table_name in ('ORDER_ITEM' , 'ORDERS') order by table_name, column_id

drop table  "SAMPLESERVICE_SERVICE"."read_side_offsets"

select * from user_tables;
select sys_context( 'userenv', 'current_schema' ) from dual;

select name, pdb, creation_date from v$services


grant create session to test20190522
grant create table to test20190522
alter user test20190522   quota unlimited on users

CREATE TABLE  "read_side_offsets" (
  "read_side_id" VARCHAR(255),
  "tag" VARCHAR(255),
  "sequence_offset" NUMBER,
  "time_uuid_offset" CHAR(36),
  PRIMARY KEY ("read_side_id", "tag")
);
