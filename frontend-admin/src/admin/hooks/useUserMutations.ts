import { useMutation, useQueryClient } from '@tanstack/react-query';
import { UserService } from '@/admin/services/UserService';
import { QueryKeys } from '@/core/config/queryClient';

export const useChangeRolMutation = () => {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: ({ userId, rol }: { userId: number; rol: 'USER' | 'ADMIN' }) =>
            UserService.cambiarRol(userId, rol),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: QueryKeys.adminUsers });
        },
    });
};

export const useChangeEstadoMutation = () => {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: ({ userId, estado }: { userId: number; estado: boolean }) =>
            UserService.cambiarEstado(userId, estado),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: QueryKeys.adminUsers });
        },
    });
};
