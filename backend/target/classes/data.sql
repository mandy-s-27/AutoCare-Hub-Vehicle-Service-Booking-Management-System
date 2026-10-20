-- Seed default services (only if table is empty)
INSERT INTO service_item (service_name, price, duration)
SELECT 'General Service', 1500.0, 60
WHERE NOT EXISTS (SELECT 1 FROM service_item WHERE service_name = 'General Service');

INSERT INTO service_item (service_name, price, duration)
SELECT 'Oil Change', 900.0, 30
WHERE NOT EXISTS (SELECT 1 FROM service_item WHERE service_name = 'Oil Change');

INSERT INTO service_item (service_name, price, duration)
SELECT 'Wheel Alignment', 600.0, 45
WHERE NOT EXISTS (SELECT 1 FROM service_item WHERE service_name = 'Wheel Alignment');

INSERT INTO service_item (service_name, price, duration)
SELECT 'Engine Check', 2500.0, 90
WHERE NOT EXISTS (SELECT 1 FROM service_item WHERE service_name = 'Engine Check');

-- Default service center
INSERT INTO service_center (name, address, city)
SELECT 'City Auto Care Center', '123 MG Road', 'Coimbatore'
WHERE NOT EXISTS (SELECT 1 FROM service_center WHERE name = 'City Auto Care Center');

-- Default admin login is seeded in code by DataSeeder.java (so the password
-- is hashed correctly with the app's PasswordEncoder at startup):
-- email: admin@vehicleservice.com / password: Admin@123
