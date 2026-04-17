export const ROUTES = {
    HOME: '/',
    OAUTH_CALLBACK: '/oauth-callback',
    AUTH: {
        LOGIN: '/login',
        REGISTER: '/registro',
        VERIFY_EMAIL: '/verificacion',
        LOGIN_PASSWORD: '/login-password',
        CHOOSE_METHOD: '/elegir-metodo',
        RECOVERY: '/recuperacion',
        VERIFY_RECOVERY: '/verificar-recuperacion',
        NEW_PASSWORD: '/nueva-password',
    },
    USER: {
        CONFIGURATION: '/configuracion',
        SESSIONS: '/sesiones',
    }
} as const;