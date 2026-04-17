import axios, { AxiosError, type InternalAxiosRequestConfig, type AxiosRequestConfig } from 'axios';
import { env } from '../config/env';
import { ApiError, type ApiErrorResponse } from '@/core/types/apiError';
import { API_ENDPOINTS } from '../constants/apiEndpoints';
import { SESSION_ERRORS } from '../constants/authConstants';
import { useAuthStore } from '@/auth/store/authStore';

let isRefreshing = false;
let sessionRevoked = false;
let failedQueue: Array<{ resolve: (value: unknown) => void; reject: (reason: unknown) => void }> = [];

export const resetSessionState = () => {
    sessionRevoked = false;
    isRefreshing = false;
    failedQueue = [];
};

const processQueue = (error: unknown, token: string | null = null) => {
    failedQueue.forEach(prom => {
        if (error) {
            prom.reject(error);
        } else {
            prom.resolve(token);
        }
    });
    failedQueue = [];
};

const axiosInstance = axios.create({
    baseURL: env.VITE_API_URL,
    timeout: 15000,
    withCredentials: true,
    headers: {
        'Content-Type': 'application/json',
    }
});

axiosInstance.interceptors.request.use((config: InternalAxiosRequestConfig) => {
    const endpoint = config.url || '';
    const isNewAuthFlow = [
        API_ENDPOINTS.AUTH.LOGIN,
        API_ENDPOINTS.AUTH.REGISTER,
    ].some(path => endpoint.includes(path));
    if (isNewAuthFlow) {
        resetSessionState();
    }

    const csrfToken = document.cookie.match(/XSRF-TOKEN=([^;]+)/)?.[1];
    if (csrfToken && config.headers) {
        config.headers['X-XSRF-TOKEN'] = decodeURIComponent(csrfToken);
    }
    return config;
}, (error) => Promise.reject(error));

axiosInstance.interceptors.response.use(
    (response) => {
        return response.data;
    },
    async (error: AxiosError<ApiErrorResponse>) => {
        const originalRequest = error.config as InternalAxiosRequestConfig & { _retry?: boolean };

        if (error.code === 'ECONNABORTED') {
            throw new Error('El servidor tardó demasiado en responder.');
        }
        if (error.message === 'Network Error' && !error.response) {
            throw new Error('Error de conexión. Verifica tu internet.');
        }

        const status = error.response?.status || 500;
        const data = error.response?.data || {};
        const endpoint = error.config?.url || '';

        const publicAuthEndpoints = [
            API_ENDPOINTS.AUTH.LOGIN,
            API_ENDPOINTS.AUTH.REGISTER,
            API_ENDPOINTS.AUTH.CHECK_EMAIL,
            API_ENDPOINTS.AUTH.VERIFY_CODE,
            API_ENDPOINTS.AUTH.RESEND_CODE,
            API_ENDPOINTS.AUTH.REQUEST_RECOVERY,
            API_ENDPOINTS.AUTH.VERIFY_RECOVERY,
            API_ENDPOINTS.AUTH.RESET_PASSWORD
        ];
        const isAuthEndpoint = publicAuthEndpoints.some(path => endpoint.includes(path));

        if (status === 401 && sessionRevoked && !isAuthEndpoint) {
            useAuthStore.getState().logout();
            throw new ApiError(401, {
                ...data,
                status: 401,
                errorCode: SESSION_ERRORS.SESSION_REVOKED
            });
        }

        if (status === 401 && !isAuthEndpoint && !endpoint.includes(API_ENDPOINTS.AUTH.REFRESH_TOKEN) && !originalRequest._retry) {

            if (isRefreshing) {
                return new Promise(function (resolve, reject) {
                    failedQueue.push({ resolve, reject });
                }).then(() => {
                    const retryConfig = { ...originalRequest, _retry: true };
                    return axiosInstance(retryConfig);
                }).catch(err => {
                    return Promise.reject(err);
                });
            }

            originalRequest._retry = true;
            isRefreshing = true;

            try {
                await axiosInstance.post(API_ENDPOINTS.AUTH.REFRESH_TOKEN);
                processQueue(null);
                isRefreshing = false;
                const retryConfig = { ...originalRequest, _retry: true };
                return axiosInstance(retryConfig);
            } catch (refreshError) {
                processQueue(refreshError, null);
                isRefreshing = false;

                sessionRevoked = true;
                throw new ApiError(401, {
                    ...data,
                    status: 401,
                    errorCode: SESSION_ERRORS.SESSION_REVOKED
                });
            }
        }

        if (status === 401) {
            const isExplicitlyRevoked =
                data.errorCode === SESSION_ERRORS.TOKEN_REVOKED ||
                data.errorCode === SESSION_ERRORS.SESSION_REVOKED;

            const isGenericAuthFailure = !isExplicitlyRevoked && (endpoint.includes('/sessions'));

            if (isExplicitlyRevoked || isGenericAuthFailure) {
                useAuthStore.getState().logout();
                throw new ApiError(401, {
                    ...data,
                    status: 401,
                    errorCode: SESSION_ERRORS.SESSION_REVOKED
                });
            }
        }

        throw new ApiError(status, { ...data, status });
    }
);

export const apiClient = {
    get: <T>(url: string, config?: AxiosRequestConfig) =>
        axiosInstance.get<T, T>(url, config),
    post: <T, D = unknown>(url: string, data?: D, config?: AxiosRequestConfig) =>
        axiosInstance.post<T, T, D>(url, data, config),
    put: <T, D = unknown>(url: string, data?: D, config?: AxiosRequestConfig) =>
        axiosInstance.put<T, T, D>(url, data, config),
    patch: <T, D = unknown>(url: string, data?: D, config?: AxiosRequestConfig) =>
        axiosInstance.patch<T, T, D>(url, data, config),
    delete: <T>(url: string, config?: AxiosRequestConfig) =>
        axiosInstance.delete<T, T>(url, config),
};