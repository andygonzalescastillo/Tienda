import { useMutation } from '@tanstack/react-query';
import { cerrarSesionService, cerrarTodasLasSesionesService } from '../services/SessionService';
import { useAuthStore } from '@/auth/store/authStore';
import { ROUTES } from '@/router/routes';

export const useSessionManager = () => {
    const logoutStore = useAuthStore((state) => state.logout);

    const logoutMutation = useMutation({
        mutationFn: (cerrarTodas: boolean = false) =>
            cerrarTodas ? cerrarTodasLasSesionesService() : cerrarSesionService(),
        onSettled: () => {
            logoutStore();
            window.location.href = ROUTES.AUTH.LOGIN;
        }
    });

    return { logout: logoutMutation };
};
