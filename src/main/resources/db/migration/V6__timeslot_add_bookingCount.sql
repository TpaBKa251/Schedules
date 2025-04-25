alter table "schedules".time_slots
    add column booking_count integer not null default 0 check ( booking_count <= "limit" );