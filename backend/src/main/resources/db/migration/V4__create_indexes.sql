-- Índices para optimizar consultas frecuentes

-- Búsqueda rápida de usuario por email (login, registro, verificación)
CREATE INDEX IF NOT EXISTS idx_usuario_email ON usuario(email);

-- Búsqueda de refresh tokens por usuario (listar sesiones activas)
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_usuario_id ON refresh_tokens(usuario_id);

-- Búsqueda de refresh tokens por token_id (validación de token)
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_token_id ON refresh_tokens(token_id);

-- Búsqueda de tokens no revocados (limpiar sesiones activas)
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_revocado ON refresh_tokens(revocado);

-- Búsqueda de proveedores por usuario (verificar vinculaciones OAuth)
CREATE INDEX IF NOT EXISTS idx_user_providers_user_id ON user_providers(user_id);

-- Índice compuesto para optimizar consultas de sesiones activas
-- Cubre: contarSesionesActivas, obtenerSesionesActivasOrdenadasPorAntiguedad
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_active_sessions ON refresh_tokens(usuario_id, revocado, fecha_expiracion);