import { QueryClient } from '@tanstack/react-query';

export const QueryKeys = {
    currentAdmin: ['currentAdmin'],
    sessions: ['sessions'],
    adminUsers: ['admin-users'],
} as const;

export const queryClient = new QueryClient({
    defaultOptions: {
        queries: {
            retry: 1,
            refetchOnWindowFocus: false,
            staleTime: 5 * 60 * 1000,
        },
    },
});
