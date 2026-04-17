export const ROUTES = {
    AUTH: {
        LOGIN: '/login',
        RECOVERY: '/recuperacion',
        VERIFY_RECOVERY: '/verificar-recuperacion',
        NEW_PASSWORD: '/nueva-password',
    },
    ADMIN: {
        DASHBOARD: '/',
        CONFIGURATION: '/configuracion',
        SESSIONS: '/sesiones',
        USERS: '/usuarios',
    },
} as const;
