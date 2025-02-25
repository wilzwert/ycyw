CREATE TABLE users (
    id UUID PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(255) NOT NULL 
);

CREATE TABLE form_message (
    id UUID PRIMARY KEY,
    support_user_id UUID NULL,
    sender_email VARCHAR(255) NOT NULL,
    sender_name VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    sent_at TIMESTAMP NOT NULL,
    replied_at TIMESTAMP,
    FOREIGN KEY (support_user_id) REFERENCES users(id) ON DELETE RESTRICT
);

CREATE TABLE chat_conversation (
    id UUID PRIMARY KEY,
    initiator_id UUID NULL,
    handler_id UUID NULL,
    initiator_username VARCHAR(255) NOT NULL,
    started_at TIMESTAMP NOT NULL,
    ended_at TIMESTAMP NOT NULL,
    FOREIGN KEY (initiator_id) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (handler_id) REFERENCES users(id) ON DELETE SET NULL
);

CREATE TABLE chat_message (
    id SERIAL PRIMARY KEY, 
    chat_conversation_id UUID NOT NULL,
    sender VARCHAR(255) NOT NULL,
    recipient VARCHAR(255) NOT NULL,
    sent_at TIMESTAMP NOT NULL,
    content TEXT NOT NULL,
    FOREIGN KEY (chat_conversation_id) REFERENCES chat_conversation(id) ON DELETE CASCADE
);

CREATE TABLE token (
    id SERIAL PRIMARY KEY,
    user_id UUID NOT NULL,
    token_string VARCHAR(512) UNIQUE NOT NULL,
    expiration TIMESTAMP NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);