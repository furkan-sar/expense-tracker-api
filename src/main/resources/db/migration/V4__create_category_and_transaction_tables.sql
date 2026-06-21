DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'categories'
          AND column_name = 'id'
          AND data_type <> 'uuid'
    ) THEN
        IF EXISTS (
            SELECT 1
            FROM information_schema.tables
            WHERE table_schema = 'public'
              AND table_name = 'legacy_categories_bigint'
        ) THEN
            DROP TABLE legacy_categories_bigint;
        END IF;

        ALTER TABLE categories RENAME TO legacy_categories_bigint;
    END IF;
END $$;

CREATE TABLE IF NOT EXISTS categories (
    id UUID PRIMARY KEY,
    budget_group_id UUID NOT NULL REFERENCES budget_groups(id) ON DELETE CASCADE,
    name VARCHAR(120) NOT NULL,
    type VARCHAR(20) NOT NULL,
    color VARCHAR(7),
    icon VARCHAR(80),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE IF NOT EXISTS transactions (
    id UUID PRIMARY KEY,
    budget_group_id UUID NOT NULL REFERENCES budget_groups(id) ON DELETE CASCADE,
    category_id UUID NOT NULL REFERENCES categories(id),
    member_id UUID NOT NULL REFERENCES budget_members(id),
    type VARCHAR(20) NOT NULL,
    amount NUMERIC(19, 2) NOT NULL,
    transaction_date DATE NOT NULL,
    note VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_categories_budget_group_id ON categories(budget_group_id);
CREATE INDEX IF NOT EXISTS idx_transactions_budget_group_id ON transactions(budget_group_id);
CREATE INDEX IF NOT EXISTS idx_transactions_category_id ON transactions(category_id);
CREATE INDEX IF NOT EXISTS idx_transactions_member_id ON transactions(member_id);
CREATE INDEX IF NOT EXISTS idx_transactions_transaction_date ON transactions(transaction_date);
