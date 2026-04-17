export const AUTH_PROVIDERS = {
    GOOGLE: 'GOOGLE',
    MICROSOFT: 'MICROSOFT',
    FACEBOOK: 'FACEBOOK',
    GITHUB: 'GITHUB',
    LOCAL: 'LOCAL',
} as const;


export const SOCIAL_PROVIDERS_LIST = [
    AUTH_PROVIDERS.GOOGLE,
    AUTH_PROVIDERS.MICROSOFT,
    AUTH_PROVIDERS.FACEBOOK,
    AUTH_PROVIDERS.GITHUB
] as const;

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
