alter table "schedules"."kitchen"
add column checked boolean not null default false,
add column schedule_number integer not null