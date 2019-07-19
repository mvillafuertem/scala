CREATE TABLE IF NOT EXISTS "user"
(
    "user_id"  VARCHAR NOT NULL,
    "name"     VARCHAR NOT NULL,
    "username" VARCHAR NOT NULL,
    "id"       BIGINT  NOT NULL PRIMARY KEY AUTO_INCREMENT
);

CREATE UNIQUE INDEX "id_userid_index" ON "user" ("id", "user_id");