CREATE TABLE IF NOT EXISTS usuario (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255),
    nombre VARCHAR(100),
    apellido VARCHAR(100),
    rol VARCHAR(50) DEFAULT 'USER',
    email_verificado BOOLEAN NOT NULL DEFAULT FALSE,
    estado BOOLEAN NOT NULL DEFAULT FALSE,
    ultima_sesion TIMESTAMP,

    -- Auditoría (AuditMetadata)
    fecha_registro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    usuario_registro VARCHAR(255),
    fecha_ultima_modificacion TIMESTAMP,
    usuario_ultima_modificacion VARCHAR(255)
);
