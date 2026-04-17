import { apiClient } from '@/core/services/apiClient';
import { API_ENDPOINTS } from '@/core/constants/apiEndpoints';
import type { MensajeResponse } from '@/core/types/commonTypes';
import type { SesionResponse } from '@/admin/types/sessionTypes';

export const cerrarSesionService = () =>
    apiClient.post<MensajeResponse>(API_ENDPOINTS.AUTH.LOGOUT);

export const cerrarTodasLasSesionesService = () =>
    apiClient.post<MensajeResponse>(API_ENDPOINTS.AUTH.LOGOUT_ALL);

export const obtenerSesionesActivasService = (signal?: AbortSignal) =>
    apiClient.get<SesionResponse[]>(API_ENDPOINTS.SESSIONS.BASE, { signal });

export const cerrarSesionEspecificaService = (tokenId: string) =>
    apiClient.delete<MensajeResponse>(`${API_ENDPOINTS.SESSIONS.BASE}/${tokenId}`);
