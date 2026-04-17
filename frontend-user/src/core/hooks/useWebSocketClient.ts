import { useContext } from 'react';
import { WebSocketContext } from '@/core/contexts/WebSocketContext';

export const useWebSocketClient = () => {
    const context = useContext(WebSocketContext);
    if (!context) {
        throw new Error('useWebSocketClient debe usarse dentro de WebSocketProvider');
    }
    return context;
};
