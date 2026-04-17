import { useEffect } from 'react';
import { useCurrentAdmin } from './useCurrentAdmin';
import { useAuthStore } from '../store/authStore';
import { useSessionSync } from './useSessionSync';
import { useSessionEvents } from './useSessionEvents';

export const useAuthSync = () => {
    const { data: usuario, isSuccess } = useCurrentAdmin();
    const setUsuario = useAuthStore((state) => state.setUsuario);

    useSessionSync();
    useSessionEvents();

    useEffect(() => {
        if (isSuccess) {
            setUsuario(usuario ?? null);
        }
    }, [usuario, isSuccess, setUsuario]);
};
