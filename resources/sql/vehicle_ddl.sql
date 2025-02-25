CREATE TABLE address (
    id SERIAL PRIMARY KEY,
    street VARCHAR(255) NOT NULL,
    city VARCHAR(100) NOT NULL,
    zip_code VARCHAR(20) NOT NULL,
    country VARCHAR(100) NOT NULL
);

CREATE TABLE agency (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    address_id INT NOT NULL UNIQUE,
    FOREIGN KEY (address_id) REFERENCES address(id) ON DELETE CASCADE
);

CREATE TABLE category (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    description TEXT,
    recommended_persons SMALLINT NOT NULL,
    parent_category_id UUID NULL,
    FOREIGN KEY (parent_category_id) REFERENCES category(id) ON DELETE SET NULL
);

CREATE TABLE Vehicle (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    category_id UUID NOT NULL,
    status VARCHAR(50) NOT NULL,
    owning_agency_id UUID NOT NULL,
    current_agency_id UUID NOT NULL,
    FOREIGN KEY (category_id) REFERENCES category(id) ON DELETE CASCADE,
    FOREIGN KEY (owning_agency_id) REFERENCES agency(id) ON DELETE CASCADE,
    FOREIGN KEY (current_agency_id) REFERENCES agency(id) ON DELETE CASCADE
);

CREATE TABLE vehicle_history (
    id SERIAL PRIMARY KEY,
    vehicle_id UUID NOT NULL,
    started_at TIMESTAMP NOT NULL,
    ended_at TIMESTAMP NOT NULL,
    start_agency_id UUID NULL,
    end_agency_id UUID NULL,
    FOREIGN KEY (vehicle_id) REFERENCES vehicle(id) ON DELETE CASCADE,
    FOREIGN KEY (start_agency_id) REFERENCES agency(id) ON DELETE SET NULL,
    FOREIGN KEY (end_agency_id) REFERENCES agency(id) ON DELETE SET NULL
);
