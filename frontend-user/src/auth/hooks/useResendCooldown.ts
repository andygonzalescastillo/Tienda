import { useState, useEffect, useCallback } from 'react';
import { useAuthFlowStore } from '../store/authFlowStore';

export const useResendCooldown = () => {
    const { getRemainingCooldown, startCooldown: storeStartCooldown } = useAuthFlowStore();

    const [cooldown, setCooldown] = useState(() => getRemainingCooldown());

    useEffect(() => {
        let timer: ReturnType<typeof setInterval>;
        if (cooldown > 0) {
            timer = setInterval(() => {
                const remaining = getRemainingCooldown();
                setCooldown(remaining);
                if (remaining <= 0) clearInterval(timer);
            }, 1000);
        }
        return () => clearInterval(timer);
    }, [cooldown, getRemainingCooldown]);

    const startCooldown = useCallback((seconds: number = 60) => {
        storeStartCooldown(seconds);
        setCooldown(seconds);
    }, [storeStartCooldown]);

    return {
        cooldown,
        startCooldown,
        isCooldownActive: cooldown > 0
    };
};
