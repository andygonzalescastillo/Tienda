import { apiClient } from '@/core/services/apiClient';
import { API_ENDPOINTS } from '@/core/constants/apiEndpoints';
import type { LoginResponse, DatosAdmin } from '../types/authTypes';

export const loginService = async (email: string, password: string) =>
    apiClient.post<LoginResponse>(API_ENDPOINTS.AUTH.LOGIN, { email, password });

export const validarSesionActualService = async (signal?: AbortSignal) =>
    apiClient.get<DatosAdmin>(API_ENDPOINTS.AUTH.VALIDATE_SESSION, { signal });
