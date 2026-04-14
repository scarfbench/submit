-- Sequences
CREATE SEQUENCE IF NOT EXISTS specialty_seq START WITH 100 INCREMENT BY 50;
CREATE SEQUENCE IF NOT EXISTS owner_pet_pettype_seq START WITH 100 INCREMENT BY 50;
CREATE SEQUENCE IF NOT EXISTS vet_seq START WITH 100 INCREMENT BY 50;
CREATE SEQUENCE IF NOT EXISTS owner_seq START WITH 100 INCREMENT BY 50;
CREATE SEQUENCE IF NOT EXISTS owner_pet_seq START WITH 100 INCREMENT BY 50;
CREATE SEQUENCE IF NOT EXISTS owner_pet_visit_seq START WITH 100 INCREMENT BY 50;

-- Specialties
INSERT INTO specialty (id, uuid, name, searchindex) VALUES (1, 'a1111111-1111-1111-1111-111111111111', 'radiology', 'radiology ');
INSERT INTO specialty (id, uuid, name, searchindex) VALUES (2, 'a2222222-2222-2222-2222-222222222222', 'surgery', 'surgery ');
INSERT INTO specialty (id, uuid, name, searchindex) VALUES (3, 'a3333333-3333-3333-3333-333333333333', 'dentistry', 'dentistry ');

-- Pet Types
INSERT INTO owner_pet_pettype (id, uuid, name, searchindex) VALUES (1, 'b1111111-1111-1111-1111-111111111111', 'cat', 'cat ');
INSERT INTO owner_pet_pettype (id, uuid, name, searchindex) VALUES (2, 'b2222222-2222-2222-2222-222222222222', 'dog', 'dog ');
INSERT INTO owner_pet_pettype (id, uuid, name, searchindex) VALUES (3, 'b3333333-3333-3333-3333-333333333333', 'lizard', 'lizard ');
INSERT INTO owner_pet_pettype (id, uuid, name, searchindex) VALUES (4, 'b4444444-4444-4444-4444-444444444444', 'snake', 'snake ');
INSERT INTO owner_pet_pettype (id, uuid, name, searchindex) VALUES (5, 'b5555555-5555-5555-5555-555555555555', 'bird', 'bird ');
INSERT INTO owner_pet_pettype (id, uuid, name, searchindex) VALUES (6, 'b6666666-6666-6666-6666-666666666666', 'hamster', 'hamster ');

-- Vets
INSERT INTO vet (id, uuid, first_name, lastname, searchindex) VALUES (1, 'c1111111-1111-1111-1111-111111111111', 'James', 'Carter', 'James Carter ');
INSERT INTO vet (id, uuid, first_name, lastname, searchindex) VALUES (2, 'c2222222-2222-2222-2222-222222222222', 'Helen', 'Leary', 'Helen Leary radiology ');
INSERT INTO vet (id, uuid, first_name, lastname, searchindex) VALUES (3, 'c3333333-3333-3333-3333-333333333333', 'Linda', 'Douglas', 'Linda Douglas dentistry surgery ');
INSERT INTO vet (id, uuid, first_name, lastname, searchindex) VALUES (4, 'c4444444-4444-4444-4444-444444444444', 'Rafael', 'Ortega', 'Rafael Ortega surgery ');
INSERT INTO vet (id, uuid, first_name, lastname, searchindex) VALUES (5, 'c5555555-5555-5555-5555-555555555555', 'Henry', 'Stevens', 'Henry Stevens radiology ');
INSERT INTO vet (id, uuid, first_name, lastname, searchindex) VALUES (6, 'c6666666-6666-6666-6666-666666666666', 'Sharon', 'Jenkins', 'Sharon Jenkins ');

-- Vet Specialties
INSERT INTO vet_specialties (vet_id, specialty_id) VALUES (2, 1);
INSERT INTO vet_specialties (vet_id, specialty_id) VALUES (3, 2);
INSERT INTO vet_specialties (vet_id, specialty_id) VALUES (3, 3);
INSERT INTO vet_specialties (vet_id, specialty_id) VALUES (4, 2);
INSERT INTO vet_specialties (vet_id, specialty_id) VALUES (5, 1);

-- Owners
INSERT INTO owner (id, uuid, first_name, lastname, address, housenumber, address_info, city, zipcode, phonenumber, email, searchindex) VALUES (1, 'd1111111-1111-1111-1111-111111111111', 'George', 'Franklin', '110 W. Liberty St.', '1', NULL, 'Madison', '53711', '+49 1234 567890', 'george.franklin@example.com', 'George Franklin 110 W Liberty St 1 Madison 53711 49 1234 567890 george franklin example com ');
INSERT INTO owner (id, uuid, first_name, lastname, address, housenumber, address_info, city, zipcode, phonenumber, email, searchindex) VALUES (2, 'd2222222-2222-2222-2222-222222222222', 'Betty', 'Davis', '638 Cardinal Ave.', '2', NULL, 'Sun Prairie', '53590', '+49 2345 678901', 'betty.davis@example.com', 'Betty Davis 638 Cardinal Ave 2 Sun Prairie 53590 49 2345 678901 betty davis example com ');
INSERT INTO owner (id, uuid, first_name, lastname, address, housenumber, address_info, city, zipcode, phonenumber, email, searchindex) VALUES (3, 'd3333333-3333-3333-3333-333333333333', 'Eduardo', 'Rodriquez', '2693 Commerce St.', '3', NULL, 'McFarland', '53558', '+49 3456 789012', 'eduardo.rodriquez@example.com', 'Eduardo Rodriquez 2693 Commerce St 3 McFarland 53558 49 3456 789012 eduardo rodriquez example com ');

-- Pets
INSERT INTO owner_pet (id, uuid, name, birth_date, owner_pet_pettype_id, owner_id, searchindex) VALUES (1, 'e1111111-1111-1111-1111-111111111111', 'Leo', '2010-09-07', 1, 1, 'Leo ');
INSERT INTO owner_pet (id, uuid, name, birth_date, owner_pet_pettype_id, owner_id, searchindex) VALUES (2, 'e2222222-2222-2222-2222-222222222222', 'Basil', '2012-08-06', 6, 2, 'Basil ');
INSERT INTO owner_pet (id, uuid, name, birth_date, owner_pet_pettype_id, owner_id, searchindex) VALUES (3, 'e3333333-3333-3333-3333-333333333333', 'Rosy', '2011-04-17', 2, 3, 'Rosy ');

-- Visits
INSERT INTO owner_pet_visit (id, uuid, visit_date, description, owner_pet_id, searchindex) VALUES (1, 'f1111111-1111-1111-1111-111111111111', '2013-01-01', 'rabies shot', 1, '2013 01 01 rabies shot ');
INSERT INTO owner_pet_visit (id, uuid, visit_date, description, owner_pet_id, searchindex) VALUES (2, 'f2222222-2222-2222-2222-222222222222', '2013-01-02', 'rabies shot', 2, '2013 01 02 rabies shot ');
INSERT INTO owner_pet_visit (id, uuid, visit_date, description, owner_pet_id, searchindex) VALUES (3, 'f3333333-3333-3333-3333-333333333333', '2013-01-03', 'neutered', 3, '2013 01 03 neutered ');
