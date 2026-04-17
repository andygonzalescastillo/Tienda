import { toast } from 'sonner';
import { SESSION_ERRORS } from "@/core/constants/authConstants";

export const ERROR_CODES = {
    INVALID_CREDENTIALS: 'INVALID_CREDENTIALS',
    USER_NOT_FOUND: 'USER_NOT_FOUND',
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
    SESSION_NOT_FOUND: 'SESSION_NOT_FOUND',
    INVALID_SESSION: 'INVALID_SESSION',
    SELF_MODIFICATION_NOT_ALLOWED: 'SELF_MODIFICATION_NOT_ALLOWED',
    USER_ALREADY_HAS_ROLE: 'USER_ALREADY_HAS_ROLE',
    USER_ALREADY_HAS_STATUS: 'USER_ALREADY_HAS_STATUS',
    USER_NO_LOCAL_ACCOUNT: 'USER_NO_LOCAL_ACCOUNT',
} as const;

export const SUCCESS_CODES = {
    LOGIN_SUCCESS: 'LOGIN_SUCCESS',
    OTP_SENT: 'OTP_SENT',
    CODE_VERIFIED: 'CODE_VERIFIED',
    PASSWORD_UPDATED: 'PASSWORD_UPDATED',
    SESSION_CLOSED: 'SESSION_CLOSED',
    CURRENT_SESSION_CLOSED: 'CURRENT_SESSION_CLOSED',
    LOGOUT_SUCCESS: 'LOGOUT_SUCCESS',
    LOGOUT_ALL_SUCCESS: 'LOGOUT_ALL_SUCCESS',
    USER_ROLE_UPDATED: 'USER_ROLE_UPDATED',
    USER_ACTIVATED: 'USER_ACTIVATED',
    USER_DEACTIVATED: 'USER_DEACTIVATED',
} as const;

interface ToastMessage {
    title: string;
    description?: string;
}

const ERROR_DICTIONARY: Record<string, ToastMessage> = {
    [ERROR_CODES.INVALID_CREDENTIALS]: { title: 'Credenciales inválidas', description: 'Verifica tu correo y contraseña e inténtalo de nuevo.' },
    [ERROR_CODES.USER_NOT_FOUND]: { title: 'Cuenta no encontrada', description: 'No se encontró una cuenta con ese correo.' },
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
    [ERROR_CODES.SESSION_NOT_FOUND]: { title: 'Sesión no encontrada' },
    [ERROR_CODES.INVALID_SESSION]: { title: 'Sesión inválida', description: 'La sesión actual no es válida o está corrompida.' },
    [ERROR_CODES.SELF_MODIFICATION_NOT_ALLOWED]: { title: 'Acción no permitida', description: 'No puedes modificar tu propia cuenta.' },
    [ERROR_CODES.USER_ALREADY_HAS_ROLE]: { title: 'Sin cambios', description: 'El usuario ya tiene ese rol asignado.' },
    [ERROR_CODES.USER_ALREADY_HAS_STATUS]: { title: 'Sin cambios', description: 'El usuario ya tiene ese estado.' },
    [ERROR_CODES.USER_NO_LOCAL_ACCOUNT]: { title: 'Cuenta local requerida', description: 'Solo se puede asignar el rol de administrador a usuarios con cuenta local (email y contraseña).' },

    'AUTH_ERROR': { title: 'Error de autenticación' },
    'ACCESS_DENIED': { title: 'Acceso denegado', description: 'No tienes permisos para realizar esta acción.' },
    'VALIDATION_ERROR': { title: 'Error de validación', description: 'Verifica los datos enviados.' },
};

const SUCCESS_DICTIONARY: Record<string, ToastMessage> = {
    [SUCCESS_CODES.LOGIN_SUCCESS]: { title: 'Inicio de sesión exitoso' },
    [SUCCESS_CODES.OTP_SENT]: { title: 'Código enviado', description: 'Revisa tu correo. Tienes {expirationMinutes} min para usarlo.' },
    [SUCCESS_CODES.CODE_VERIFIED]: { title: 'Código verificado' },
    [SUCCESS_CODES.PASSWORD_UPDATED]: { title: 'Contraseña actualizada', description: 'Ya puedes iniciar sesión con tu nueva contraseña.' },
    [SUCCESS_CODES.SESSION_CLOSED]: { title: 'Sesión cerrada' },
    [SUCCESS_CODES.CURRENT_SESSION_CLOSED]: { title: 'Sesión actual cerrada' },
    [SUCCESS_CODES.LOGOUT_SUCCESS]: { title: 'Sesión cerrada correctamente' },
    [SUCCESS_CODES.LOGOUT_ALL_SUCCESS]: { title: 'Todas las sesiones cerradas' },
    [SUCCESS_CODES.USER_ROLE_UPDATED]: { title: 'Rol actualizado', description: 'El rol del usuario ha sido actualizado correctamente.' },
    [SUCCESS_CODES.USER_ACTIVATED]: { title: 'Usuario activado' },
    [SUCCESS_CODES.USER_DEACTIVATED]: { title: 'Usuario desactivado' },
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
