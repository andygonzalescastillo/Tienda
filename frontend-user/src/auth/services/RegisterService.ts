import { apiClient } from '@/core/services/apiClient';
import { API_ENDPOINTS } from '@/core/constants/apiEndpoints';
import type { VerificarEmailResponse, RegisterResponse, LoginResponse } from '../types/authTypes';
import type { MensajeResponse } from '@/core/types/commonTypes';

export const verificarEmailExistenteService = async (email: string) =>
    apiClient.post<VerificarEmailResponse>(API_ENDPOINTS.AUTH.CHECK_EMAIL, { email });

export const registrarService = async (email: string, password: string, nombre: string, apellido: string) =>
    apiClient.post<RegisterResponse>(API_ENDPOINTS.AUTH.REGISTER, { email, password, nombre, apellido });

export const verificarCodigoService = async (email: string, codigo: string) =>
    apiClient.post<LoginResponse>(API_ENDPOINTS.AUTH.VERIFY_CODE, { email, codigo });

export const reenviarCodigoService = async (email: string) =>
    apiClient.post<MensajeResponse>(API_ENDPOINTS.AUTH.RESEND_CODE, { email });
