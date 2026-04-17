import { useQuery } from '@tanstack/react-query';
import { QueryKeys } from '@/core/config/queryClient';
import { validarSesionActualService } from '../services/LoginService';
import { useEstaAutenticado } from '../store/authStore';
import type { DatosUsuario } from '../types/authTypes';

export const useCurrentUser = () => {
    const estaAutenticado = useEstaAutenticado();

    return useQuery({
        queryKey: QueryKeys.currentUser,
        queryFn: async ({ signal }): Promise<DatosUsuario | null> => {
            try {
                const { email, nombre, rol } = await validarSesionActualService(signal);
                return { email, nombre: nombre || '', rol };
            } catch {
                return null;
            }
        },
        enabled: estaAutenticado,
        retry: 1,
    });
};
