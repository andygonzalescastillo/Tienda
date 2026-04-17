CREATE TABLE IF NOT EXISTS refresh_tokens (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    token_id VARCHAR(255) NOT NULL UNIQUE,
    usuario_id BIGINT NOT NULL,
    token_hash VARCHAR(255) NOT NULL,
    fecha_expiracion TIMESTAMP NOT NULL,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ip_address VARCHAR(45),
    user_agent TEXT,
    ubicacion VARCHAR(150),
    revocado BOOLEAN NOT NULL DEFAULT FALSE,
    fecha_revocacion TIMESTAMP,
    razon_revocacion VARCHAR(100),
    ultimo_acceso TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_refresh_usuario FOREIGN KEY (usuario_id)
        REFERENCES usuario(id) ON DELETE CASCADE
);
