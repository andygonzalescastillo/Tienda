import { useEffect, useCallback, useRef } from 'react';
import { ROUTES } from '@/router/routes';
import { useAuthStore } from '../store/authStore';
import { queryClient, QueryKeys } from '@/core/config/queryClient';

export const useSessionSync = () => {
    const logoutStore = useAuthStore((state) => state.logout);
    const channelRef = useRef<BroadcastChannel | null>(null);

    const handleLogout = useCallback(() => {
        logoutStore();
        channelRef.current?.postMessage('LOGOUT');
        window.location.href = ROUTES.AUTH.LOGIN;
    }, [logoutStore]);

    const broadcastLogin = useCallback(() => {
        channelRef.current?.postMessage('LOGIN');
    }, []);

    useEffect(() => {
        const channel = new BroadcastChannel('admin_auth_channel');
        channelRef.current = channel;

        channel.onmessage = (event) => {
            if (event.data === 'LOGOUT') {
                logoutStore();
                window.location.href = ROUTES.AUTH.LOGIN;
            } else if (event.data === 'LOGIN') {
                queryClient.invalidateQueries({ queryKey: QueryKeys.currentAdmin });
            }
        };

        const handleStorage = (e: StorageEvent) => {
            if (e.key === 'admin-auth-storage') {
                const newValue = e.newValue;
                if (newValue) {
                    try {
                        const parsed = JSON.parse(newValue);
                        if (parsed?.state?.usuario) {
                            useAuthStore.setState({ usuario: parsed.state.usuario });
                        } else if (!parsed?.state?.usuario) {
                            logoutStore();
                            window.location.href = ROUTES.AUTH.LOGIN;
                        }
                    } catch (error) {
                        console.error("Error parsing auth storage", error);
                    }
                }
            }
        };

        window.addEventListener('storage', handleStorage);

        return () => {
            channel.close();
            channelRef.current = null;
            window.removeEventListener('storage', handleStorage);
        };
    }, [logoutStore]);

    return { logout: handleLogout, broadcastLogin };
};
