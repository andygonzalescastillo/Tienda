import { useMutation } from '@tanstack/react-query';
import { solicitarRecuperacionService, verificarCodigoRecuperacionService, restablecerPasswordService } from '@/auth/services/RecoveryService';

export const useRequestPasswordRecovery = () => useMutation({ mutationFn: (email: string) => solicitarRecuperacionService(email) });

export const useVerifyRecoveryCode = () => useMutation({
    mutationFn: ({ email, codigo }: { email: string; codigo: string }) => verificarCodigoRecuperacionService(email, codigo)
});

export const useResetPassword = () => useMutation({
    mutationFn: ({ email, codigo, nuevaPassword }: { email: string; codigo: string; nuevaPassword: string }) => restablecerPasswordService(email, codigo, nuevaPassword)
});