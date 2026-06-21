CREATE TABLE IF NOT EXISTS budget_groups (
    id UUID PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    description VARCHAR(500),
    currency VARCHAR(3) NOT NULL,
    owner_user_id UUID NOT NULL REFERENCES users(id),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE IF NOT EXISTS budget_members (
    id UUID PRIMARY KEY,
    budget_group_id UUID NOT NULL REFERENCES budget_groups(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role VARCHAR(20) NOT NULL,
    joined_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_budget_members_group_user UNIQUE (budget_group_id, user_id)
);

CREATE INDEX IF NOT EXISTS idx_budget_groups_owner_user_id ON budget_groups(owner_user_id);
CREATE INDEX IF NOT EXISTS idx_budget_members_user_id ON budget_members(user_id);
CREATE INDEX IF NOT EXISTS idx_budget_members_budget_group_id ON budget_members(budget_group_id);
