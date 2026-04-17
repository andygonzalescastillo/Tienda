import { create } from 'zustand';
import { persist, createJSONStorage } from 'zustand/middleware';

interface AuthFlowState {
    email: string;
    from: string;
    proveedoresVinculados: string[] | null;
    codigoRecuperacion: string;
    cooldownExpiry: number | null;

    setEmail: (email: string) => void;
    setFrom: (from: string) => void;
    setFlow: (data: { email: string; from?: string }) => void;
    setProveedores: (p: string[] | null) => void;
    setCodigoRecuperacion: (c: string) => void;
    startCooldown: (seconds: number) => void;
    getRemainingCooldown: () => number;
    limpiar: () => void;
}

export const useAuthFlowStore = create<AuthFlowState>()(
    persist(
        (set, get) => ({
            email: '',
            from: '/',
            proveedoresVinculados: null,
            codigoRecuperacion: '',
            cooldownExpiry: null,

            setEmail: (email) => set({ email }),
            setFrom: (from) => set({ from }),
            setFlow: ({ email, from }) =>
                set({ email, ...(from !== undefined ? { from } : {}) }),
            setProveedores: (proveedoresVinculados) => set({ proveedoresVinculados }),
            setCodigoRecuperacion: (codigoRecuperacion) => set({ codigoRecuperacion }),

            startCooldown: (seconds) =>
                set({ cooldownExpiry: Date.now() + seconds * 1000 }),

            getRemainingCooldown: () => {
                const expiry = get().cooldownExpiry;
                if (!expiry) return 0;
                const remaining = Math.ceil((expiry - Date.now()) / 1000);
                return remaining > 0 ? remaining : 0;
            },

            limpiar: () =>
                set({
                    email: '',
                    from: '/',
                    proveedoresVinculados: null,
                    codigoRecuperacion: '',
                    cooldownExpiry: null,
                }),
        }),
        {
            name: 'auth-flow',
            storage: createJSONStorage(() => sessionStorage),
        }
    )
);
