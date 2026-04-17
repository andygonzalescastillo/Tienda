import { useState } from 'react';
import { useNavigate, Navigate } from 'react-router-dom';
import { ROUTES } from '@/router/routes';
import { useAdminAuthFlowStore } from '@/auth/store/adminAuthFlowStore';
import { useVerifyRecoveryCode, useRequestPasswordRecovery } from '@/auth/hooks/useRecoveryMutations';
import { showSuccessToast } from '@/core/utils/messageMapper';
import { handleApiError } from '@/core/utils/handleApiError';
import { LoginContainer } from '@/auth/components/layout/LoginContainer';
import { AdminVerifyRecoveryForm } from '@/auth/components/forms/AdminVerifyRecoveryForm';
import { useResendCooldown } from '@/auth/hooks/useResendCooldown';
import { StepProgress } from '@/components/custom/StepProgress';
import { RECOVERY_STEPS } from '@/core/constants/authConstants';
import { toast } from 'sonner';

export const AdminVerifyRecovery = () => {
    const navigate = useNavigate();
    const { recoveryEmail, setRecoveryCode } = useAdminAuthFlowStore();

    const verifyMutation = useVerifyRecoveryCode();
    const requestRecoveryMutation = useRequestPasswordRecovery();
    const [codigo, setCodigo] = useState('');
    const { cooldown, startCooldown } = useResendCooldown();

    if (!recoveryEmail) {
        return <Navigate to={ROUTES.AUTH.LOGIN} replace />;
    }

    const handleVerify = async () => {
        if (codigo.length !== 6) {
            return toast.error('Código incompleto', { description: 'Ingresa los 6 dígitos del código enviado a tu correo.' });
        }

        try {
            await verifyMutation.mutateAsync({ email: recoveryEmail, codigo });
            toast.success('Código verificado', { description: 'Ahora crea tu nueva contraseña.' });
            setRecoveryCode(codigo);
            setTimeout(() => navigate(ROUTES.AUTH.NEW_PASSWORD), 1000);
        } catch (error) {
            handleApiError(error);
        }
    };

    const handleReenviar = async () => {
        if (cooldown > 0) return;

        try {
            const res = await requestRecoveryMutation.mutateAsync(recoveryEmail);
            showSuccessToast(res.successCode, res.metadata);
            setCodigo('');
            startCooldown(60);
        } catch (error) {
            handleApiError(error);
        }
    };

    return (
        <LoginContainer titulo="Autenticación requerida">
            <StepProgress steps={RECOVERY_STEPS} currentStep={1} />
            <AdminVerifyRecoveryForm
                email={recoveryEmail}
                codigo={codigo}
                onCodigoChange={setCodigo}
                cargando={verifyMutation.isPending || requestRecoveryMutation.isPending}
                onVerificar={handleVerify}
                onReenviar={handleReenviar}
                cooldown={cooldown}
            />
        </LoginContainer>
    );
};
