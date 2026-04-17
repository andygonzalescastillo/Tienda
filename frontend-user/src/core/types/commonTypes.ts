export interface MensajeResponse {
    successCode?: string;
    metadata?: Record<string, unknown>;
}

export interface SessionEvent {
    type: 'SESSION_REVOKED' | 'SESSIONS_UPDATED' | 'FORCE_LOGOUT';
    payload: string | null;
    timestamp: string;
}
