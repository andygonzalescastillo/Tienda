import { createContext } from 'react';
import { type Client } from '@stomp/stompjs';

export interface WebSocketContextType {
    clientRef: React.RefObject<Client | null>;
    connected: boolean;
}

export const WebSocketContext = createContext<WebSocketContextType | null>(null);
