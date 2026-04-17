import { create } from 'zustand';
import { devtools, persist, createJSONStorage } from 'zustand/middleware';
import type { DatosUsuario } from '../types/authTypes';
import { queryClient, QueryKeys } from '@/core/config/queryClient';
import { resetSessionState } from '@/core/services/apiClient';

interface AuthState {
    usuario: DatosUsuario | null;
    setUsuario: (usuario: DatosUsuario | null) => void;
    login: (usuario: DatosUsuario) => void;
    logout: () => void;
}

export const useAuthStore = create<AuthState>()(
    devtools(
        persist(
            (set) => ({
                usuario: null,
                setUsuario: (usuario) => set({ usuario }),
                login: (usuario) => {
                    resetSessionState();
                    set({ usuario });
                    queryClient.invalidateQueries({ queryKey: QueryKeys.currentUser });
                },
                logout: () => {
                    resetSessionState();
                    set({ usuario: null });
                    queryClient.removeQueries({ queryKey: QueryKeys.currentUser });
                    queryClient.removeQueries({ queryKey: QueryKeys.sessions });
                },
            }),
            {
                name: 'auth-storage',
                storage: createJSONStorage(() => localStorage),
            }
        ),
        { name: 'AuthStore', enabled: import.meta.env.DEV }
    )
);

export const useUsuario = () => useAuthStore((state) => state.usuario);

export const useEstaAutenticado = () => useAuthStore((state) => !!state.usuario);