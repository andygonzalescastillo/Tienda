import { create } from 'zustand';
import { devtools, persist, createJSONStorage } from 'zustand/middleware';
import type { DatosAdmin } from '../types/authTypes';
import { queryClient, QueryKeys } from '@/core/config/queryClient';
import { resetSessionState } from '@/core/services/apiClient';

interface AuthState {
    usuario: DatosAdmin | null;
    setUsuario: (usuario: DatosAdmin | null) => void;
    login: (usuario: DatosAdmin) => void;
    logout: () => void;
}

export const useAuthStore = create<AuthState>()(
    devtools(
        persist(
            (set) => ({
                usuario: null,
                setUsuario: (usuario: DatosAdmin | null) => set({ usuario }),
                login: (usuario: DatosAdmin) => {
                    resetSessionState();
                    set({ usuario });
                    queryClient.invalidateQueries({ queryKey: QueryKeys.currentAdmin });
                },
                logout: () => {
                    resetSessionState();
                    set({ usuario: null });
                    queryClient.removeQueries({ queryKey: QueryKeys.currentAdmin });
                    queryClient.removeQueries({ queryKey: QueryKeys.sessions });
                },
            }),
            {
                name: 'admin-auth-storage',
                storage: createJSONStorage(() => localStorage),
            }
        ),
        { name: 'AdminAuthStore', enabled: import.meta.env.DEV }
    )
);

export const useUsuario = () => useAuthStore((state) => state.usuario);

export const useEstaAutenticado = () => useAuthStore((state) => !!state.usuario);
