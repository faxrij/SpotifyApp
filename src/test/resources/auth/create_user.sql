INSERT INTO users (id, name, last_name, email, password_hash, role, is_verified,created_date,modified_date)
    VALUES ('39d525c8-25a5-4b7c-b6e1-6aa0132cf104', 'memberName', 'memberLastName', 'member@gmail.com', '$2a$10$Dy.de69P9YcCwZTHOodMJe0vo7v2DlwjgthOuAm9bSkEh/1BOzB3C',
        'MEMBER', true, now(), now());

INSERT INTO users (id, name, last_name, email, password_hash, role, is_verified,created_date,modified_date)
VALUES ('39d525c8-25a5-4b7c-b6e1-6aa0132cf102', 'adminName', 'adminLastName', 'admin@gmail.com', '$2a$10$Dy.de69P9YcCwZTHOodMJe0vo7v2DlwjgthOuAm9bSkEh/1BOzB3C',
        'ADMIN', true, now(), now());