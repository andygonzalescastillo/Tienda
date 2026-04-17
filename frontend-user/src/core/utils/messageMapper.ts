import { toast } from 'sonner';
import { SESSION_ERRORS } from "@/core/constants/authConstants";

export const ERROR_CODES = {
    INVALID_CREDENTIALS: 'INVALID_CREDENTIALS',
    EMAIL_ALREADY_VERIFIED: 'EMAIL_ALREADY_VERIFIED',
    EMAIL_NOT_VERIFIED: 'EMAIL_NOT_VERIFIED',
    EMAIL_ERROR: 'EMAIL_ERROR',
    USER_NOT_FOUND: 'USER_NOT_FOUND',
    OAUTH_ACCOUNT: 'OAUTH_ACCOUNT',
    VERIFICATION_CODE_EXPIRED: 'VERIFICATION_CODE_EXPIRED',
    MAX_ATTEMPTS_EXCEEDED: 'MAX_ATTEMPTS_EXCEEDED',
    INVALID_VERIFICATION_CODE: 'INVALID_VERIFICATION_CODE',
    UNAUTHORIZED: 'UNAUTHORIZED',
    SESSION_REVOKED: SESSION_ERRORS.SESSION_REVOKED,
    INVALID_REFRESH_TOKEN: 'INVALID_REFRESH_TOKEN',
    INVALID_TOKEN_TYPE: 'INVALID_TOKEN_TYPE',
    REFRESH_TOKEN_NOT_FOUND: 'REFRESH_TOKEN_NOT_FOUND',
    CANNOT_CLOSE_CURRENT_SESSION: 'CANNOT_CLOSE_CURRENT_SESSION',
    SESSION_ACCESS_DENIED: 'SESSION_ACCESS_DENIED',
    CRYPTO_CONFIG_ERROR: 'CRYPTO_CONFIG_ERROR',
    OAUTH2_PROVIDER_ID_MISSING: 'OAUTH2_PROVIDER_ID_MISSING',
    OAUTH2_AUTHENTICATION_FAILED: 'OAUTH2_AUTHENTICATION_FAILED',
    OAUTH2_STRATEGY_NOT_FOUND: 'OAUTH2_STRATEGY_NOT_FOUND',
    OAUTH2_UNSUPPORTED_PROVIDER: 'OAUTH2_UNSUPPORTED_PROVIDER',
    SESSION_NOT_FOUND: 'SESSION_NOT_FOUND',
    INVALID_SESSION: 'INVALID_SESSION',
} as const;

export const SUCCESS_CODES = {
    REGISTER_SUCCESS: 'REGISTER_SUCCESS',
    CODE_VERIFIED: 'CODE_VERIFIED',
    CODE_RESENT: 'CODE_RESENT',
    OTP_SENT: 'OTP_SENT',
    PASSWORD_UPDATED: 'PASSWORD_UPDATED',
    SESSION_CLOSED: 'SESSION_CLOSED',
    CURRENT_SESSION_CLOSED: 'CURRENT_SESSION_CLOSED',
    LOGOUT_SUCCESS: 'LOGOUT_SUCCESS',
    LOGOUT_ALL_SUCCESS: 'LOGOUT_ALL_SUCCESS',
} as const;

interface ToastMessage {
    title: string;
    description?: string;
}

const ERROR_DICTIONARY: Record<string, ToastMessage> = {
    [ERROR_CODES.INVALID_CREDENTIALS]: { title: 'Credenciales inválidas', description: 'Verifica tu correo y contraseña e inténtalo de nuevo.' },
    [ERROR_CODES.EMAIL_ALREADY_VERIFIED]: { title: 'Correo ya registrado', description: 'Este correo ya está registrado y verificado.' },
    [ERROR_CODES.EMAIL_NOT_VERIFIED]: { title: 'Correo no verificado', description: 'Revisa tu bandeja de entrada y verifica tu email para continuar.' },
    [ERROR_CODES.EMAIL_ERROR]: { title: 'Error de correo', description: 'No se pudo procesar la solicitud para este correo.' },
    [ERROR_CODES.USER_NOT_FOUND]: { title: 'Cuenta no encontrada', description: 'No se encontró una cuenta con ese correo.' },
    [ERROR_CODES.OAUTH_ACCOUNT]: { title: 'Cuenta vinculada', description: 'Esta cuenta usa inicio de sesión con {provider}. Usa ese método para acceder.' },
    [ERROR_CODES.VERIFICATION_CODE_EXPIRED]: { title: 'Código expirado', description: 'Solicita uno nuevo. El código tiene una validez de {expirationMinutes} min.' },
    [ERROR_CODES.MAX_ATTEMPTS_EXCEEDED]: { title: 'Intentos agotados', description: 'Has excedido el máximo de {maxAttempts} intentos. Solicita un nuevo código.' },
    [ERROR_CODES.INVALID_VERIFICATION_CODE]: { title: 'Código incorrecto', description: 'Te quedan {remainingAttempts} intentos.' },
    [ERROR_CODES.UNAUTHORIZED]: { title: 'Sesión expirada', description: 'Tu sesión ha expirado. Inicia sesión nuevamente.' },
    [ERROR_CODES.SESSION_REVOKED]: { title: 'Sesión cerrada', description: 'Tu sesión ha sido cerrada desde otro dispositivo.' },
    [ERROR_CODES.INVALID_REFRESH_TOKEN]: { title: 'Token inválido', description: 'El token de actualización es inválido o ha expirado.' },
    [ERROR_CODES.INVALID_TOKEN_TYPE]: { title: 'Token inválido', description: 'El tipo de token proporcionado no es válido.' },
    [ERROR_CODES.REFRESH_TOKEN_NOT_FOUND]: { title: 'Sesión no encontrada', description: 'No se encontró una sesión activa para renovar.' },
    [ERROR_CODES.CANNOT_CLOSE_CURRENT_SESSION]: { title: 'Acción no permitida', description: 'No puedes cerrar tu sesión actual desde la gestión de dispositivos.' },
    [ERROR_CODES.SESSION_ACCESS_DENIED]: { title: 'Acceso denegado', description: 'No tienes permiso para modificar esta sesión.' },
    [ERROR_CODES.CRYPTO_CONFIG_ERROR]: { title: 'Error interno' },
    [ERROR_CODES.OAUTH2_PROVIDER_ID_MISSING]: { title: 'Error de configuración', description: 'No se proporcionó el proveedor de inicio de sesión social.' },
    [ERROR_CODES.OAUTH2_AUTHENTICATION_FAILED]: { title: 'Autenticación fallida', description: 'Falló la autenticación con el proveedor social.' },
    [ERROR_CODES.OAUTH2_STRATEGY_NOT_FOUND]: { title: 'Proveedor no configurado', description: 'Este proveedor de inicio de sesión social no está configurado.' },
    [ERROR_CODES.OAUTH2_UNSUPPORTED_PROVIDER]: { title: 'Proveedor no soportado', description: 'Este método de inicio de sesión social no está disponible.' },
    [ERROR_CODES.SESSION_NOT_FOUND]: { title: 'Sesión no encontrada' },
    [ERROR_CODES.INVALID_SESSION]: { title: 'Sesión inválida', description: 'La sesión actual no es válida o está corrompida.' },

    'AUTH_ERROR': { title: 'Error de autenticación' },
    'ACCESS_DENIED': { title: 'Acceso denegado', description: 'No tienes permisos para realizar esta acción.' },
    'VALIDATION_ERROR': { title: 'Error de validación', description: 'Verifica los datos enviados.' },
};

const SUCCESS_DICTIONARY: Record<string, ToastMessage> = {
    [SUCCESS_CODES.REGISTER_SUCCESS]: { title: '¡Registro exitoso!', description: 'El código de verificación tiene una validez de {expirationMinutes} min.' },
    [SUCCESS_CODES.CODE_VERIFIED]: { title: 'Código verificado' },
    [SUCCESS_CODES.CODE_RESENT]: { title: 'Código reenviado', description: 'Revisa tu correo. Tienes {expirationMinutes} min para usarlo.' },
    [SUCCESS_CODES.OTP_SENT]: { title: 'Código enviado', description: 'Revisa tu correo. Tienes {expirationMinutes} min para usarlo.' },
    [SUCCESS_CODES.PASSWORD_UPDATED]: { title: 'Contraseña actualizada', description: 'Ya puedes iniciar sesión con tu nueva contraseña.' },
    [SUCCESS_CODES.SESSION_CLOSED]: { title: 'Sesión cerrada' },
    [SUCCESS_CODES.CURRENT_SESSION_CLOSED]: { title: 'Sesión actual cerrada' },
    [SUCCESS_CODES.LOGOUT_SUCCESS]: { title: 'Sesión cerrada correctamente' },
    [SUCCESS_CODES.LOGOUT_ALL_SUCCESS]: { title: 'Todas las sesiones cerradas' },
};


const interpolate = (text: string, params: Record<string, unknown> = {}): string => {
    return text.replace(/{(\w+)}/g, (_, key) => {
        return params[key]?.toString() ?? `{${key}}`;
    });
};

const resolve = (dict: Record<string, ToastMessage>, code: string, params: Record<string, unknown> = {}, fallback: string): ToastMessage => {
    const entry = dict[code];
    if (!entry) return { title: fallback };
    return {
        title: interpolate(entry.title, params),
        description: entry.description ? interpolate(entry.description, params) : undefined,
    };
};

export const showSuccessToast = (code?: string, params: Record<string, unknown> = {}) => {
    const { title, description } = resolve(SUCCESS_DICTIONARY, code || '', params, 'Operación exitosa');
    toast.success(title, { description });
};

export const showErrorToast = (code?: string, params: Record<string, unknown> = {}) => {
    const { title, description } = resolve(ERROR_DICTIONARY, code || '', params, 'Ha ocurrido un error inesperado');
    toast.error(title, { description });
};

export const getSuccessMessage = (code?: string, params: Record<string, unknown> = {}): string => {
    const { title } = resolve(SUCCESS_DICTIONARY, code || '', params, 'Operación exitosa');
    return title;
};

export const getErrorMessage = (code?: string, params: Record<string, unknown> = {}): string => {
    const { title } = resolve(ERROR_DICTIONARY, code || '', params, 'Ha ocurrido un error inesperado');
    return title;
};
