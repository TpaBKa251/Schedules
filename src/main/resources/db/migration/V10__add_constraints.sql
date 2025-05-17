alter table "schedules"."responsibles"
    add constraint uq_responsible_type_and_user unique ("type", "user", "date");