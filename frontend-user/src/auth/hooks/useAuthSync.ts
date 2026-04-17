import { useEffect } from 'react';
import { useCurrentUser } from './useCurrentUser';
import { useAuthStore } from '../store/authStore';
import { useSessionSync } from './useSessionSync';
import { useSessionEvents } from './useSessionEvents';

export const useAuthSync = () => {
    const { data: usuario, isSuccess } = useCurrentUser();
    const setUsuario = useAuthStore((state) => state.setUsuario);

    useSessionSync();
    useSessionEvents();

    useEffect(() => {
        if (isSuccess) {
            setUsuario(usuario ?? null);
        }
    }, [usuario, isSuccess, setUsuario]);
};
