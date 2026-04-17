import { apiClient } from '@/core/services/apiClient';
import { API_ENDPOINTS } from '@/core/constants/apiEndpoints';
import type { AdminUserPage, UserFilters } from '@/admin/types/userTypes';
import type { MensajeResponse } from '@/core/types/commonTypes';

export const UserService = {
    listarUsuarios: async (filters: UserFilters): Promise<AdminUserPage> => {
        const params = new URLSearchParams();
        params.set('page', String(filters.page));
        params.set('size', String(filters.size));
        if (filters.search) params.set('search', filters.search);
        if (filters.rol) params.set('rol', filters.rol);
        if (filters.estado) params.set('estado', filters.estado);

        return apiClient.get<AdminUserPage>(
            `${API_ENDPOINTS.ADMIN.USERS}?${params.toString()}`
        );
    },

    cambiarRol: async (userId: number, rol: 'USER' | 'ADMIN'): Promise<MensajeResponse> => {
        return apiClient.patch<MensajeResponse>(
            `${API_ENDPOINTS.ADMIN.USERS}/${userId}/rol`,
            { rol }
        );
    },

    cambiarEstado: async (userId: number, estado: boolean): Promise<MensajeResponse> => {
        return apiClient.patch<MensajeResponse>(
            `${API_ENDPOINTS.ADMIN.USERS}/${userId}/estado`,
            { estado }
        );
    },
};
