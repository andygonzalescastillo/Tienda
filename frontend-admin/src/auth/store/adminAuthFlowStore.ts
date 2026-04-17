import { create } from 'zustand';
import { persist, createJSONStorage } from 'zustand/middleware';

interface AdminAuthFlowState {
    recoveryEmail: string;
    recoveryCode: string;
    cooldownExpiry: number | null;

    setRecoveryEmail: (email: string) => void;
    setRecoveryCode: (code: string) => void;
    startCooldown: (seconds: number) => void;
    getRemainingCooldown: () => number;
    clearFlow: () => void;
}

export const useAdminAuthFlowStore = create<AdminAuthFlowState>()(
    persist(
        (set, get) => ({
            recoveryEmail: '',
            recoveryCode: '',
            cooldownExpiry: null,

            setRecoveryEmail: (email: string) => set({ recoveryEmail: email }),
            setRecoveryCode: (code: string) => set({ recoveryCode: code }),

            startCooldown: (seconds) =>
                set({ cooldownExpiry: Date.now() + seconds * 1000 }),

            getRemainingCooldown: () => {
                const expiry = get().cooldownExpiry;
                if (!expiry) return 0;
                const remaining = Math.ceil((expiry - Date.now()) / 1000);
                return remaining > 0 ? remaining : 0;
            },

            clearFlow: () => set({
                recoveryEmail: '',
                recoveryCode: '',
                cooldownExpiry: null,
            }),
        }),
        {
            name: 'admin-auth-flow-storage',
            storage: createJSONStorage(() => sessionStorage),
        }
    )
);
