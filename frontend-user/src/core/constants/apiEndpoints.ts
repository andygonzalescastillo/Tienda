export const API_ENDPOINTS = {
    AUTH: {
        LOGIN: '/auth/login',
        REGISTER: '/auth/register',
        CHECK_EMAIL: '/auth/verificar-email',
        VERIFY_CODE: '/auth/verificar-codigo',
        RESEND_CODE: '/auth/reenviar-codigo',
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
    }
} as const;