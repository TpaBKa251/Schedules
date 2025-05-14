alter table "schedules"."kitchen"
    add column version bigint not null default 0;