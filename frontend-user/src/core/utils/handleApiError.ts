import { ApiError } from '@/core/types/apiError';
import { showErrorToast } from '@/core/utils/messageMapper';

export const handleApiError = (error: unknown) => {
    if (error instanceof ApiError) {
        showErrorToast(error.errorCode, error.metadata);
    }
};
