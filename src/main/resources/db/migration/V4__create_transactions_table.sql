CREATE TABLE transactions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    type VARCHAR(20) NOT NULL, -- 'fund', 'convert', 'trade'
    from_currency VARCHAR(10),
    to_currency VARCHAR(10),
    amount NUMERIC(20, 2) NOT NULL,
    rate_used NUMERIC(15, 6),
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);