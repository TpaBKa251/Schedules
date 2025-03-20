ALTER TABLE "schedules"."kitchen"
    ADD CONSTRAINT chk_kitchen_schedule_number_positive CHECK (schedule_number > 0);