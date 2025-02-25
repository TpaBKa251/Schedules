CREATE TABLE "schedules"."time_slots"
(
    id         UUID                        NOT NULL UNIQUE,
    start_time TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    end_time   TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    type       VARCHAR(255)                NOT NULL,
    "limit"    INTEGER                     NOT NULL,
    CONSTRAINT pk_time_slots PRIMARY KEY (id)
);