CREATE TABLE "schedules"."responsibles"
(
    id     UUID         NOT NULL UNIQUE PRIMARY KEY,
    "type" VARCHAR(255) NOT NULL,
    "date" DATE         NOT NULL,
    "user" UUID,
    CONSTRAINT uq_responsible_type_and_date UNIQUE ("type", "date")
);