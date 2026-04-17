import { useQuery, keepPreviousData } from '@tanstack/react-query';
import { UserService } from '@/admin/services/UserService';
import type { UserFilters } from '@/admin/types/userTypes';
import { QueryKeys } from '@/core/config/queryClient';

export const useUsersQuery = (filters: UserFilters) =>
    useQuery({
        queryKey: [...QueryKeys.adminUsers, filters],
        queryFn: () => UserService.listarUsuarios(filters),
        placeholderData: keepPreviousData,
    });
