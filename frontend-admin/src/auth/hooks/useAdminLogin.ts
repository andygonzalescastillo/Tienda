import { useMutation } from '@tanstack/react-query';
import { loginService } from '../services/LoginService';

export const useAdminLogin = () => useMutation({
    mutationFn: ({ email, password }: { email: string; password: string }) => loginService(email, password)
});
