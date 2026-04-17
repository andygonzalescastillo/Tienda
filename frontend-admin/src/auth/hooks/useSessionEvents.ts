import { useCallback } from 'react';
import { useSubscription } from '@/core/hooks/useSubscription';
import { useAuthStore } from '@/auth/store/authStore';
import { queryClient, QueryKeys } from '@/core/config/queryClient';
import { ROUTES } from '@/router/routes';
import type { SessionEvent } from '@/core/types/commonTypes';

export const useSessionEvents = () => {
    const logout = useAuthStore((state) => state.logout);

    const handleSessionEvent = useCallback((event: SessionEvent) => {

        switch (event.type) {
            case 'SESSION_REVOKED':
                queryClient.invalidateQueries({ queryKey: QueryKeys.sessions });
                queryClient.invalidateQueries({ queryKey: QueryKeys.currentAdmin });
                break;

            case 'SESSIONS_UPDATED':
                queryClient.invalidateQueries({ queryKey: QueryKeys.sessions });
                break;

            case 'USERS_UPDATED':
                queryClient.invalidateQueries({ queryKey: QueryKeys.adminUsers });
                break;

            case 'FORCE_LOGOUT':
                logout();
                window.location.href = ROUTES.AUTH.LOGIN;
                break;
        }
    }, [logout]);

    useSubscription<SessionEvent>('/user/queue/session-events', {
        onMessage: handleSessionEvent,
    });
};
