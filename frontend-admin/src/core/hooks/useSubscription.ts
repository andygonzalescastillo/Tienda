import { useEffect, useRef, useState, useCallback } from 'react';
import { type IMessage } from '@stomp/stompjs';
import { useWebSocketClient } from '@/core/hooks/useWebSocketClient';

interface UseSubscriptionOptions<T> {
    onMessage?: (data: T) => void;
    enabled?: boolean;
}

export const useSubscription = <T = unknown>(
    destination: string,
    options: UseSubscriptionOptions<T> = {}
) => {
    const { clientRef, connected } = useWebSocketClient();
    const [lastMessage, setLastMessage] = useState<T | null>(null);
    const onMessageRef = useRef(options.onMessage);
    const enabled = options.enabled ?? true;

    useEffect(() => {
        onMessageRef.current = options.onMessage;
    }, [options.onMessage]);

    const handleMessage = useCallback((stompMessage: IMessage) => {
        try {
            const data = JSON.parse(stompMessage.body) as T;
            setLastMessage(data);
            onMessageRef.current?.(data);
        } catch {
            // JSON parse fallido, ignorar mensaje malformado
        }
    }, []);

    useEffect(() => {
        if (!enabled || !connected) return;

        const client = clientRef.current;
        if (!client?.connected) return;

        const subscription = client.subscribe(destination, handleMessage);

        return () => {
            subscription.unsubscribe();
        };
    }, [destination, enabled, connected, handleMessage, clientRef]);

    return { lastMessage };
};
