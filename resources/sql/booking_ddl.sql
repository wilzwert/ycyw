CREATE TABLE booking (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    vehicle_category_id UUID NULL,
    vehicle_category_name VARCHAR(255) NOT NULL,
    preferred_vehicle_id UUID DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    start_at TIMESTAMP NOT NULL,
    end_at TIMESTAMP NOT NULL,
    start_agency_id UUID NOT NULL,
    end_agency_id UUID NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    status VARCHAR(50) NOT NULL
);