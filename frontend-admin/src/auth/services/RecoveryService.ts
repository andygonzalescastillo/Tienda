import { apiClient } from '@/core/services/apiClient';
import { API_ENDPOINTS } from '@/core/constants/apiEndpoints';
import type { MensajeResponse } from '@/core/types/commonTypes';

export const solicitarRecuperacionService = async (email: string) =>
    apiClient.post<MensajeResponse>(API_ENDPOINTS.AUTH.REQUEST_RECOVERY, { email });

export const verificarCodigoRecuperacionService = async (email: string, codigo: string) =>
    apiClient.post<MensajeResponse>(API_ENDPOINTS.AUTH.VERIFY_RECOVERY, { email, codigo });

export const restablecerPasswordService = async (email: string, codigo: string, nuevaPassword: string) =>
    apiClient.post<MensajeResponse>(API_ENDPOINTS.AUTH.RESET_PASSWORD, { email, codigo, nuevaPassword });
