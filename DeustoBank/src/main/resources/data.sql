-- ============================================================
-- DATOS DE PRUEBA - DeustoBank
-- ============================================================

-- USUARIOS
INSERT INTO users (id, email, password, dni, full_name, phone, role, active, failed_login_attempts, last_login)
SELECT 1, '1@gmail.com', '$2a$10$rPa2DrQn.BK/ECNPOe39/ecVVtr3F6Oo2We.TIFL5C95PFrm7yRni', '1', '1', null, 'USER', true, 0, null
WHERE NOT EXISTS (SELECT 1 FROM users WHERE dni = '1');

INSERT INTO users (id, email, password, dni, full_name, phone, role, active, failed_login_attempts, last_login)
SELECT 2, 'Admin@deustobank.com', '$2a$10$ykaN.K5j.ZJYL6p7C0XC6eoWksVo2lWG2DyAki6rDZrQlj/pbtjmC', 'Admin', 'Admin', null, 'ADMIN', true, 0, null
WHERE NOT EXISTS (SELECT 1 FROM users WHERE dni = 'Admin');

INSERT INTO users (id, email, password, dni, full_name, phone, role, active, failed_login_attempts, last_login)
SELECT 3, '2@gmail.com', '$2a$10$09cTgE2wBAwj1CU7/yUZzurpUMMoeV3Ff10pDBi0wBjiFrWjSq1OO', '2', '2', null, 'USER', true, 0, null
WHERE NOT EXISTS (SELECT 1 FROM users WHERE dni = '2');

INSERT INTO users (id, email, password, dni, full_name, phone, role, active, failed_login_attempts, last_login)
SELECT 4, '3@gmail.com', '$2a$10$F1zj5btmT393U1XZQoXZM..bwR4lMurrU1bcgFOb2WcXloE1FrCc6', '3', '3', null, 'USER', true, 0, null
WHERE NOT EXISTS (SELECT 1 FROM users WHERE dni = '3');