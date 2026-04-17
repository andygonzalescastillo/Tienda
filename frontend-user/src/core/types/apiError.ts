export interface ApiErrorResponse {
    type?: string;
    title?: string;
    status?: number;
    detail?: string;
    instance?: string;
    errorCode?: string;
    errores?: Record<string, string>;
    metadata?: Record<string, unknown>;
}

export class ApiError extends Error {
    public status: number;
    public data: ApiErrorResponse;

    constructor(status: number, data: ApiErrorResponse) {
        super(data.title || 'API Error');
        this.status = status;
        this.data = data;
        this.name = 'ApiError';
    }

    get errorCode() { return this.data.errorCode; }
    get errores() { return this.data.errores; }
    get metadata() { return this.data.metadata || {}; }
}