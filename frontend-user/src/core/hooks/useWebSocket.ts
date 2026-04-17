import { useEffect, useRef, useCallback, useState } from 'react';
import { Client } from '@stomp/stompjs';
import { env } from '@/core/config/env';
import { useEstaAutenticado } from '@/auth/store/authStore';
import { apiClient } from '@/core/services/apiClient';
import { useAuthStore } from '@/auth/store/authStore';

export const useWebSocket = () => {
    const clientRef = useRef<Client | null>(null);
    const [connected, setConnected] = useState(false);
    const estaAutenticado = useEstaAutenticado();

    const connect = useCallback(() => {
        if (clientRef.current?.active) return;

        const wsUrl = env.VITE_WS_URL;

        const client = new Client({
            brokerURL: wsUrl,
            reconnectDelay: 5000,
            heartbeatIncoming: 10000,
            heartbeatOutgoing: 10000,
            beforeConnect: () => {
                return new Promise<void>(async (resolve) => {
                    try {
                        const res = await apiClient.get<{ successCode: string }>('/auth/ws-token');
                        if (res.successCode) {
                            client.connectHeaders = {
                                Authorization: `Bearer ${res.successCode}`
                            };
                        }
                    } catch (error: unknown) {
                        const status = (error as { response?: { status?: number } })?.response?.status;
                        if (status === 401) {
                            try {
                                await apiClient.post('/auth/refresh');
                                const res = await apiClient.get<{ successCode: string }>('/auth/ws-token');
                                if (res.successCode) {
                                    client.connectHeaders = {
                                        Authorization: `Bearer ${res.successCode}`
                                    };
                                }
                            } catch {
                                client.connectHeaders = {};
                                client.deactivate();
                                useAuthStore.getState().logout();
                            }
                        } else {
                            client.connectHeaders = {};
                        }
                    }
                    resolve();
                });
            },
            onConnect: () => {
                setConnected(true);
            },
            onDisconnect: () => {
                setConnected(false);
            },
            onStompError: () => {
                setConnected(false);
            },
            onWebSocketClose: () => {
                setConnected(false);
            },
        });

        clientRef.current = client;
        client.activate();
    }, []);

    const deactivateClient = useCallback(() => {
        if (clientRef.current?.active) {
            clientRef.current.deactivate();
        }
        clientRef.current = null;
    }, []);

    useEffect(() => {
        if (estaAutenticado) {
            connect();
        } else {
            deactivateClient();
        }
        return () => {
            deactivateClient();
        };
    }, [estaAutenticado, connect, deactivateClient]);

    return { clientRef, connected };
};

