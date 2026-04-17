CREATE TABLE IF NOT EXISTS user_providers (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id BIGINT NOT NULL,
    provider_name VARCHAR(20) NOT NULL,
    provider_id VARCHAR(255) NOT NULL,

    CONSTRAINT fk_provider_user FOREIGN KEY (user_id)
        REFERENCES usuario(id) ON DELETE CASCADE,

    CONSTRAINT uk_user_provider UNIQUE (user_id, provider_name)
);
