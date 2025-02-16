-- $2a$12$g1Rn9WaB49fIwT/xX1Z5..euhI7mR0.61IfeARObS9iWOvkebMp/O
-- INSERT INTO USERS (ID, USERNAME, PASSWORD, ROLE) VALUES
--    ('43e77e35-4e73-4192-a607-3f509a2feda9', 'support', '$2a$12$g1Rn9WaB49fIwT/xX1Z5..euhI7mR0.61IfeARObS9iWOvkebMp/O', 'SUPPORT');

MERGE INTO USERS (ID, USERNAME, PASSWORD, ROLE)
VALUES
    ('43e77e35-4e73-4192-a607-3f509a2feda9', 'support', '$2a$12$g1Rn9WaB49fIwT/xX1Z5..euhI7mR0.61IfeARObS9iWOvkebMp/O', 'SUPPORT'),
    ('d2e4909c-01d0-4b49-bcdc-6eba5c35d106', 'agent', '$2a$12$g1Rn9WaB49fIwT/xX1Z5..euhI7mR0.61IfeARObS9iWOvkebMp/O', 'SUPPORT'),
    ('c66cc166-5883-4cc2-9fdd-219aacdeb7d0', 'client', '$2a$12$g1Rn9WaB49fIwT/xX1Z5..euhI7mR0.61IfeARObS9iWOvkebMp/O', 'USER');