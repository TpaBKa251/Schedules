alter table "schedules"."time_slots"
    add column version bigint not null default 0;

alter table "schedules"."responsibles"
    add column version bigint not null default 0;