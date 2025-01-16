CREATE SCHEMA IF NOT EXISTS "schedules";

create table "schedules"."kitchen"
(
    id          uuid primary key unique not null,
    "date"      date                    not null,
    room_number varchar(255)            not null,
    constraint uq_schedules_date_and_roomNumber unique ("date", room_number)
)

