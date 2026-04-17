import { useState, useCallback } from 'react';
import { env } from '@/core/config/env';

const OAUTH_BASE = env.VITE_BACKEND_URL || '';

export const useOAuthProviders = ({ email: defaultEmail }: { email?: string }) => {
    const [cargando, setCargando] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const redirect = useCallback((provider: string, queryParams: string = '') => {
        setCargando(true);
        setError(null);
        window.location.href = `${OAUTH_BASE}/oauth2/authorization/${provider}${queryParams}`;
    }, []);

    const encode = encodeURIComponent;

    const buildHint = (email?: unknown) => {
        const e = (typeof email === 'string' ? email : undefined) || defaultEmail;
        return e ? `?login_hint=${encode(e)}` : '';
    };

    const buildGitHubHint = (email?: unknown) => {
        const e = (typeof email === 'string' ? email : undefined) || defaultEmail;
        return e ? `?login=${encode(e)}` : '';
    };

    return {
        cargando, error, setError, setCargando,
        handleGoogleLogin: (email?: unknown) => redirect('google', buildHint(email)),
        handleMicrosoftLogin: (email?: unknown) => redirect('microsoft', buildHint(email)),
        handleFacebookLogin: () => redirect('facebook'),
        handleGitHubLogin: (email?: unknown) => redirect('github', buildGitHubHint(email)),
    };
};
