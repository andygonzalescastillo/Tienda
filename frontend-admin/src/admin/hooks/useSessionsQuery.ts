import { useQuery } from '@tanstack/react-query';
import { obtenerSesionesActivasService } from '../services/SessionService';
import { QueryKeys } from '@/core/config/queryClient';
import { useEstaAutenticado } from '@/auth/store/authStore';

export const useSessionsQuery = () => {
    const estaAutenticado = useEstaAutenticado();

    return useQuery({
        queryKey: QueryKeys.sessions,
        queryFn: ({ signal }) => obtenerSesionesActivasService(signal),
        enabled: estaAutenticado,
        staleTime: Infinity,
    });
};
