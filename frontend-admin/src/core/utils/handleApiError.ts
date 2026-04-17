import { ApiError } from '@/core/types/apiError';
import { showErrorToast } from '@/core/utils/messageMapper';
import { SESSION_ERRORS } from '@/core/constants/authConstants';

const SILENT_ERROR_CODES = [
    'UNAUTHORIZED',
    SESSION_ERRORS.SESSION_REVOKED,
    'REFRESH_TOKEN_NOT_FOUND',
];

export const handleApiError = (error: unknown) => {
    if (error instanceof ApiError) {
        if (SILENT_ERROR_CODES.includes(error.errorCode || '')) return;
        showErrorToast(error.errorCode, error.metadata);
    }
};
