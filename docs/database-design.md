# Database Design

## users

Fields:

* id
* email
* password_hash
* first_name
* last_name
* created_at
* updated_at

## budget_groups

Fields:

* id
* name
* owner_id
* created_at
* updated_at

## budget_members

Fields:

* id
* budget_group_id
* user_id
* role
* created_at

Roles:

* OWNER
* ADMIN
* MEMBER

## categories

Fields:

* id
* budget_group_id
* name
* type
* icon
* color
* created_at

Types:

* INCOME
* EXPENSE

## transactions

Fields:

* id
* budget_group_id
* category_id
* created_by
* type
* amount
* description
* transaction_date
* created_at
* updated_at

Types:

* INCOME
* EXPENSE

## refresh_tokens

Fields:

* id
* user_id
* token
* expires_at
* created_at
