-- ==========================
-- Food types

/*
* Because of the menu_id column (@OneToOne), eventually we’ll also need to UPDATE these rows to assign a menu (once you show me your Menu entity).
* For now, I left menu_id empty so the rows will insert successfully.
*/
-- ==========================
INSERT INTO food_types (type_name, description) VALUES
                                                    ('A1', 'Almennt fæði'),
                                                    ('A2', 'Hentar eldri kynslóðinni'),
                                                    ('A3', 'Grænmetisfæði'),
                                                    ('OP', 'Orku og próteinbætt fæði'),
                                                    ('RDS-KF', 'RDS kjöt/fiskur'),
                                                    ('RDS-G', 'RDS grænmetisfæði'),
                                                    ('M1', 'Mjúkt'),
                                                    ('M2', 'Hakkað'),
                                                    ('M3', 'Fínmaukað'),
                                                    ('F1', 'Fljótandi fæði'),
                                                    ('F1-S', 'F1 Sykurskert'),
                                                    ('F1-M', 'F1 Mjólkurlaust'),
                                                    ('F2', 'Tært fljótandi'),
                                                    ('F3', 'Fljótandi fæði eftir aðgerð'),
                                                    ('F4', 'Þykkfljótandi fæði'),
                                                    ('F4-S', 'F4 Sykurskert'),
                                                    ('F5', 'Fljótandi fæði kalt'),
                                                    ('FS40', 'Fituskert 40g'),
                                                    ('PR50', 'Próteinskert 50g'),
                                                    ('PR50-S', 'Próteinskert 50g Sykurskert'),
                                                    ('PR60', 'Próteinskert 60g'),
                                                    ('SA', 'Saltskert'),
                                                    ('SA-S', 'Salskert sykurskert'),
                                                    ('BL', 'Blóðskilunarfæði'),
                                                    ('BL-S', 'Blóðskilunarfæði Sykurskert'),
                                                    ('FSMS', 'FSMS'),
                                                    ('MS', 'Mjólkursykurskert'),
                                                    ('GL', 'Glútensnautt'),
                                                    ('ÖV', 'Örveruskert'),
                                                    ('JO', 'Joðsnautt'),
                                                    ('UB', 'Ungbarnafæði'),
                                                    ('FA', 'FASTANDI'),
                                                    ('EIN', 'EINNOTA');

-- ==========================
-- Wards
-- ==========================
INSERT INTO wards (ward_name, password) VALUES
                                            ('Cardiology', 'secret'),
                                            ('D2', 'abc');

-- ==========================
-- Meals
-- ==========================
INSERT INTO meals (name, description, category, food_type_id) VALUES
                                                                  ('Oatmeal', 'Oats, milk, raisins', 'Breakfast', 1),
                                                                  ('Chicken Soup', 'Chicken, vegetables', 'Lunch', 1),
                                                                  ('Vegetable Salad', 'Lettuce, tomato, cucumber', 'Lunch', 3),
                                                                  ('Fish Stew', 'Cod, potatoes, carrots', 'Dinner', 1),
                                                                  ('Gluten-Free Pasta', 'Rice pasta with vegetables', 'Dinner', 4);

-- ==========================
-- Menus
    -- erum ekki með snacks núna
-- ==========================
INSERT INTO menus (date, breakfast_id, lunch_id, dinner_id) VALUES
                                                                (CURRENT_DATE, 1, 2, 4),  -- General diet
                                                                (CURRENT_DATE, 1, 3, 5);  -- Vegetarian diet

-- Link FoodTypes to Menus
UPDATE food_types SET menu_id = 1 WHERE type_name = 'A1';
UPDATE food_types SET menu_id = 2 WHERE type_name = 'A3';

-- ==========================
-- Patients
-- ==========================
INSERT INTO patients (name, age, bed_number, foodtype_id, ward_id) VALUES
                                                                       ('Sunna', 22, 1, 3, 1), -- A3 vegetarian
                                                                       ('Silja', 12, 2, 1, 1), -- A1 general
                                                                       ('Anna', 65, 3, 2, 2),  -- A2 elderly
                                                                       ('Jon', 45, 4, 4, 2);   -- OP gluten-free