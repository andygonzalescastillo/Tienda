import { useMutation } from '@tanstack/react-query';
import { useAuthStore } from '@/auth/store/authStore';
import { reenviarCodigoService, verificarCodigoService } from '@/auth/services/RegisterService';
import type { LoginResponse } from '@/auth/types/authTypes';

export const useVerifyEmailCode = () => {
    const storeLogin = useAuthStore((state) => state.login);

    return useMutation({
        mutationFn: ({ email, codigo }: { email: string; codigo: string }) =>
            verificarCodigoService(email, codigo),
        onSuccess: (data: LoginResponse) => {
            storeLogin({ email: data.email, nombre: data.nombre, rol: data.rol });
        }
    });
};

export const useResendEmailCode = () =>
    useMutation({ mutationFn: (email: string) => reenviarCodigoService(email) });