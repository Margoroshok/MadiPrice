-- MediPrice Sample Test Data
-- Passwords are BCrypt hashed: admin123 / user123

-- Admin user (password: admin123)
INSERT OR IGNORE INTO users (username, email, password_hash, role) VALUES
('admin', 'admin@mediprice.com', '$2a$12$XJB.YYj.NkGBYp/Y8Y7.BOj5h2NAUH8Hqz7sUf5mXIbkRTbHB9hbm', 'ADMIN');

-- Regular users (password: user123)
INSERT OR IGNORE INTO users (username, email, password_hash, role) VALUES
('john_doe', 'john@example.com', '$2a$12$Yw3kTI3/qnkEaBOjnU9MregUetRTsWdrW0LqwmVT1DsgQIK6CBSae', 'USER'),
('jane_smith', 'jane@example.com', '$2a$12$Yw3kTI3/qnkEaBOjnU9MregUetRTsWdrW0LqwmVT1DsgQIK6CBSae', 'USER'),
('alice_w', 'alice@example.com', '$2a$12$Yw3kTI3/qnkEaBOjnU9MregUetRTsWdrW0LqwmVT1DsgQIK6CBSae', 'USER');

-- Medicines
INSERT OR IGNORE INTO medicines (name, manufacturer, description) VALUES
('Paracetamol 500mg', 'PharmaCo', 'Analgesic and antipyretic. Used for mild to moderate pain and fever.'),
('Ibuprofen 400mg', 'MedLab', 'Anti-inflammatory, analgesic and antipyretic drug.'),
('Amoxicillin 500mg', 'BioPharm', 'Broad-spectrum antibiotic for bacterial infections.'),
('Omeprazole 20mg', 'GastroMed', 'Proton pump inhibitor for acid reflux and gastric ulcers.'),
('Metformin 500mg', 'DiabCare', 'First-line medication for type 2 diabetes.'),
('Atorvastatin 20mg', 'CardioLife', 'Statin medication to lower cholesterol and triglycerides.'),
('Cetirizine 10mg', 'AllerFree', 'Second-generation antihistamine for allergic reactions.'),
('Aspirin 100mg', 'PharmaCo', 'Antiplatelet agent, also used as analgesic and anti-inflammatory.'),
('Lisinopril 10mg', 'CardioLife', 'ACE inhibitor for hypertension and heart failure.'),
('Vitamin D3 1000IU', 'VitaPlus', 'Supplement for vitamin D deficiency and bone health.'),
('Azithromycin 500mg', 'BioPharm', 'Antibiotic for respiratory, skin and sexually transmitted infections.'),
('Loratadine 10mg', 'AllerFree', 'Antihistamine for allergies, hay fever and hives.'),
('Pantoprazole 40mg', 'GastroMed', 'Proton pump inhibitor for erosive esophagitis.'),
('Metoprolol 50mg', 'CardioLife', 'Beta blocker for hypertension, angina and heart failure.'),
('Doxycycline 100mg', 'MedLab', 'Tetracycline antibiotic for various bacterial infections.');

-- Pharmacies
INSERT OR IGNORE INTO pharmacies (name, address, city, latitude, longitude, phone) VALUES
('City Pharmacy Central', 'Main Street 1', 'Warsaw', 52.2297, 21.0122, '+48-22-100-1001'),
('Healthy Life Pharmacy', 'Park Avenue 15', 'Warsaw', 52.2350, 21.0200, '+48-22-100-1002'),
('MedExpress Pharmacy', 'Hospital Road 7', 'Warsaw', 52.2200, 20.9900, '+48-22-100-1003'),
('PharmaPlus North', 'Northern Blvd 42', 'Warsaw', 52.2500, 21.0050, '+48-22-100-1004'),
('Quick Meds', 'Shopping Center 3', 'Warsaw', 52.2150, 21.0300, '+48-22-100-1005'),
('GreenCross Pharmacy', 'University Street 8', 'Krakow', 50.0647, 19.9450, '+48-12-200-2001'),
('MediStore Krakow', 'Royal Road 22', 'Krakow', 50.0600, 19.9500, '+48-12-200-2002'),
('Health Hub', 'Central Square 5', 'Gdansk', 54.3520, 18.6466, '+48-58-300-3001'),
('Pharma24', 'Harbor Street 11', 'Gdansk', 54.3600, 18.6600, '+48-58-300-3002'),
('MedFirst', 'Tech Park 99', 'Wroclaw', 51.1079, 17.0385, '+48-71-400-4001');

-- Medicine prices (medicine_id, pharmacy_id, price, quantity)
INSERT OR IGNORE INTO medicine_prices (medicine_id, pharmacy_id, price, quantity) VALUES
-- Paracetamol 500mg (id=1)
(1, 1, 4.99, 150), (1, 2, 5.49, 80), (1, 3, 4.50, 200), (1, 4, 5.99, 60), (1, 5, 4.75, 120),
-- Ibuprofen 400mg (id=2)
(2, 1, 7.99, 100), (2, 2, 8.49, 50), (2, 3, 7.50, 180), (2, 4, 8.99, 30), (2, 5, 7.75, 90),
-- Amoxicillin 500mg (id=3)
(3, 1, 15.99, 40), (3, 2, 16.99, 25), (3, 3, 14.50, 60), (3, 6, 15.50, 35), (3, 7, 16.50, 20),
-- Omeprazole 20mg (id=4)
(4, 1, 12.99, 80), (4, 2, 13.49, 45), (4, 4, 12.00, 100), (4, 5, 13.99, 55), (4, 6, 11.99, 70),
-- Metformin 500mg (id=5)
(5, 1, 9.99, 60), (5, 3, 10.49, 40), (5, 5, 9.50, 90), (5, 8, 10.99, 30), (5, 9, 9.75, 50),
-- Atorvastatin 20mg (id=6)
(6, 2, 22.99, 35), (6, 3, 23.49, 20), (6, 4, 21.99, 50), (6, 7, 24.99, 15), (6, 10, 22.50, 40),
-- Cetirizine 10mg (id=7)
(7, 1, 8.99, 120), (7, 2, 9.49, 70), (7, 3, 8.50, 160), (7, 5, 9.99, 80), (7, 6, 8.25, 100),
-- Aspirin 100mg (id=8)
(8, 1, 5.99, 200), (8, 2, 6.49, 150), (8, 4, 5.50, 250), (8, 8, 6.99, 100), (8, 9, 5.75, 130),
-- Lisinopril 10mg (id=9)
(9, 3, 14.99, 45), (9, 5, 15.49, 30), (9, 7, 14.50, 55), (9, 9, 15.99, 20), (9, 10, 14.25, 65),
-- Vitamin D3 1000IU (id=10)
(10, 1, 18.99, 90), (10, 2, 19.49, 60), (10, 3, 18.50, 110), (10, 6, 19.99, 40), (10, 10, 17.99, 75),
-- Azithromycin 500mg (id=11)
(11, 1, 19.99, 30), (11, 3, 20.49, 20), (11, 5, 19.50, 40), (11, 8, 21.99, 10), (11, 10, 19.25, 25),
-- Loratadine 10mg (id=12)
(12, 2, 7.99, 100), (12, 4, 8.49, 60), (12, 5, 7.50, 140), (12, 7, 8.99, 45), (12, 9, 7.25, 110),
-- Pantoprazole 40mg (id=13)
(13, 1, 16.99, 50), (13, 3, 17.49, 35), (13, 6, 16.50, 65), (13, 8, 17.99, 25), (13, 10, 16.25, 55),
-- Metoprolol 50mg (id=14)
(14, 2, 11.99, 70), (14, 4, 12.49, 45), (14, 5, 11.50, 90), (14, 9, 12.99, 30), (14, 10, 11.25, 80),
-- Doxycycline 100mg (id=15)
(15, 1, 13.99, 40), (15, 3, 14.49, 25), (15, 7, 13.50, 55), (15, 8, 14.99, 15), (15, 10, 13.25, 45);

-- Sample reservations
INSERT OR IGNORE INTO reservations (user_id, medicine_id, pharmacy_id, quantity, status, reservation_date) VALUES
(2, 1, 1, 2, 'COMPLETED', datetime('now', '-10 days')),
(2, 3, 3, 1, 'COMPLETED', datetime('now', '-7 days')),
(2, 7, 2, 1, 'CANCELLED', datetime('now', '-5 days')),
(2, 10, 1, 2, 'PENDING', datetime('now', '-1 day')),
(3, 2, 3, 1, 'CONFIRMED', datetime('now', '-3 days')),
(3, 5, 5, 2, 'PENDING', datetime('now', '-2 days')),
(4, 4, 4, 1, 'COMPLETED', datetime('now', '-14 days')),
(4, 8, 2, 3, 'CONFIRMED', datetime('now', '-1 day'));
