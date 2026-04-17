export const RECOVERY_STEPS = [
    { label: 'Email' },
    { label: 'Código' },
    { label: 'Nueva contraseña' },
] as const;

export const SESSION_ERRORS = {
    TOKEN_REVOKED: 'TOKEN_REVOKED',
    SESSION_REVOKED: 'SESSION_REVOKED',
    INVALID_TOKEN: 'INVALID_TOKEN',
    INVALID_SESSION: 'INVALID_SESSION'
} as const;
