import type { ReactNode } from 'react';
import { WebSocketContext } from '@/core/contexts/WebSocketContext';
import { useWebSocket } from '@/core/hooks/useWebSocket';

interface WebSocketProviderProps {
    children: ReactNode;
}

export const WebSocketProvider = ({ children }: WebSocketProviderProps) => {
    const { clientRef, connected } = useWebSocket();

    return (
        <WebSocketContext.Provider value={{ clientRef, connected }}>
            {children}
        </WebSocketContext.Provider>
    );
};
