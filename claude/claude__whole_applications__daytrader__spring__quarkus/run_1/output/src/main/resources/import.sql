-- DayTrader initial seed data for Quarkus/Hibernate
-- The keygenejb table is used by KeySequenceDirect for ID generation
-- This table is NOT a JPA entity, so we need to create it manually

CREATE TABLE IF NOT EXISTS KEYGENEJB (
  KEYVAL INTEGER NOT NULL,
  KEYNAME VARCHAR(250) NOT NULL,
  CONSTRAINT PK_KEYGENEJB PRIMARY KEY (KEYNAME)
);

MERGE INTO KEYGENEJB (KEYNAME, KEYVAL) KEY(KEYNAME) VALUES ('account', 0);
MERGE INTO KEYGENEJB (KEYNAME, KEYVAL) KEY(KEYNAME) VALUES ('holding', 0);
MERGE INTO KEYGENEJB (KEYNAME, KEYVAL) KEY(KEYNAME) VALUES ('order', 0);
