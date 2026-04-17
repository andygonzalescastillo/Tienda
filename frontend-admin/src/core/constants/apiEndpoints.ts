export const API_ENDPOINTS = {
    AUTH: {
        LOGIN: '/auth/login',
        REQUEST_RECOVERY: '/auth/solicitar-recuperacion',
        VERIFY_RECOVERY: '/auth/verificar-codigo-recuperacion',
        RESET_PASSWORD: '/auth/restablecer-password',
        REFRESH_TOKEN: '/auth/refresh',
        VALIDATE_SESSION: '/auth/validate',
        LOGOUT: '/auth/logout',
        LOGOUT_ALL: '/auth/logout-all',
    },
    SESSIONS: {
        BASE: '/auth/sessions',
    },
    ADMIN: {
        USERS: '/api/admin/usuarios',
    },
} as const;
