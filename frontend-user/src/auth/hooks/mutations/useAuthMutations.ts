import { loginService } from '@/auth/services/LoginService';
import { registrarService, verificarEmailExistenteService } from '@/auth/services/RegisterService';
import { useAuthStore } from '@/auth/store/authStore';
import { useMutation } from '@tanstack/react-query';
import type { LoginResponse } from '@/auth/types/authTypes';

export const useLogin = () => {
    const storeLogin = useAuthStore((state) => state.login);
    const logout = useAuthStore((state) => state.logout);

    return useMutation({
        mutationFn: ({ email, password }: { email: string; password: string }) =>
            loginService(email, password),
        onSuccess: (data: LoginResponse) => {
            storeLogin({ email: data.email, nombre: data.nombre, rol: data.rol });
        },
        onError: () => {
            logout();
        }
    });
};

export const useRegister = () =>
    useMutation({
        mutationFn: ({ email, password, nombre, apellido }: {
            email: string; password: string; nombre: string; apellido: string
        }) => registrarService(email, password, nombre, apellido)
    });

export const useCheckEmail = () =>
    useMutation({ mutationFn: (email: string) => verificarEmailExistenteService(email) });
