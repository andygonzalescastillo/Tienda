import { useMutation } from '@tanstack/react-query';
import { cerrarSesionService, cerrarTodasLasSesionesService, cerrarSesionEspecificaService } from '../services/SessionService';
import { SESSION_ERRORS } from '@/core/constants/authConstants';
import { queryClient, QueryKeys } from '@/core/config/queryClient';
import { useAuthStore } from '@/auth/store/authStore';
import { ApiError } from '@/core/types/apiError';
import { ROUTES } from '@/router/routes';

export const useSessionManager = () => {
    const logoutStore = useAuthStore((state) => state.logout);

    const logoutMutation = useMutation({
        mutationFn: (cerrarTodas: boolean = false) =>
            cerrarTodas ? cerrarTodasLasSesionesService() : cerrarSesionService(),
        onSettled: () => {
            logoutStore();
            window.location.href = ROUTES.HOME;
        }
    });

    return { logout: logoutMutation };
};

export const useCloseSessionMutation = () => {
    const logout = useAuthStore((s) => s.logout);

    return useMutation({
        mutationFn: async (tokenId: string) => ({
            response: await cerrarSesionEspecificaService(tokenId),
            tokenId
        }),
        onSuccess: ({ response }) => {
            queryClient.invalidateQueries({ queryKey: QueryKeys.sessions });
            if (response.successCode === 'CURRENT_SESSION_CLOSED') {
                logout();
            }
        },
        onError: (err: unknown) => {
            const error = err as ApiError;
            const isSessionRevoked =
                error?.errorCode === SESSION_ERRORS.TOKEN_REVOKED ||
                error?.errorCode === SESSION_ERRORS.SESSION_REVOKED;

            if (isSessionRevoked) {
                logout();
            }
        }
    });
};
