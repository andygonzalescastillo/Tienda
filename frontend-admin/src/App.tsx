import { RouterProvider } from 'react-router-dom';
import { QueryClientProvider } from '@tanstack/react-query';
import { ReactQueryDevtools } from '@tanstack/react-query-devtools';
import { router } from '@/router/AppRoutes';
import { Toaster } from '@/components/ui/sonner';
import { TooltipProvider } from '@/components/ui/tooltip';
import { useAuthSync } from '@/auth/hooks/useAuthSync';
import { queryClient } from '@/core/config/queryClient';
import { ThemeProvider } from '@/components/theme/ThemeProvider';
import { WebSocketProvider } from '@/core/providers/WebSocketProvider';

const AuthSynchronizer = () => {
  useAuthSync();
  return null;
};

export default function App() {
  return (
    <ThemeProvider defaultTheme="system" storageKey="admin-theme">
      <QueryClientProvider client={queryClient}>
        <TooltipProvider>
          <WebSocketProvider>
            <AuthSynchronizer />
            <RouterProvider router={router} />
            <Toaster position="bottom-right" richColors closeButton />
            {import.meta.env.DEV && <ReactQueryDevtools initialIsOpen={false} />}
          </WebSocketProvider>
        </TooltipProvider>
      </QueryClientProvider>
    </ThemeProvider>
  );
}
