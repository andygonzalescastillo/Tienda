import { useQuery } from '@tanstack/react-query';
import { QueryKeys } from '@/core/config/queryClient';
import { validarSesionActualService } from '../services/LoginService';
import { useEstaAutenticado } from '../store/authStore';
import type { DatosAdmin } from '../types/authTypes';

export const useCurrentAdmin = () => {
    const estaAutenticado = useEstaAutenticado();

    return useQuery({
        queryKey: QueryKeys.currentAdmin,
        queryFn: async ({ signal }): Promise<DatosAdmin | null> => {
            try {
                const { email, nombre, rol } = await validarSesionActualService(signal);
                if (rol !== 'ADMIN') return null;
                return { email, nombre: nombre || '', rol };
            } catch {
                return null;
            }
        },
        enabled: estaAutenticado,
        retry: 1,
    });
};
