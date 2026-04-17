import { useState } from 'react';
import { useNavigate, Navigate } from 'react-router-dom';
import { toast } from 'sonner';
import { ROUTES } from '@/router/routes';
import { LoginContainer } from '../components/layout/LoginContainer';
import { VerificationForm } from '../components/forms/VerificationForm';
import { useRequestPasswordRecovery, useVerifyRecoveryCode } from '../hooks/mutations/useRecoveryMutations';
import { useResendCooldown } from '../hooks/useResendCooldown';
import { handleApiError } from '@/core/utils/handleApiError';
import { showSuccessToast } from '@/core/utils/messageMapper';
import { StepProgress } from '@/components/custom/StepProgress';
import { RECOVERY_STEPS } from '@/core/constants/authConstants';
import { useAuthFlowStore } from '../store/authFlowStore';

export const UserVerifyRecovery = () => {
    const navigate = useNavigate();
    const { email, setCodigoRecuperacion } = useAuthFlowStore();

    const verifyRecoveryMutation = useVerifyRecoveryCode();
    const requestRecoveryMutation = useRequestPasswordRecovery();
    const [codigo, setCodigo] = useState("");
    const { cooldown, startCooldown } = useResendCooldown();

    const handleVerificar = async () => {
        if (codigo.length !== 6) {
            return toast.error('Código incompleto', { description: 'Ingresa los 6 dígitos del código enviado a tu correo.' });
        }

        try {
            await verifyRecoveryMutation.mutateAsync({ email, codigo });
            setCodigoRecuperacion(codigo);
            toast.success('Código verificado', { description: 'Ahora crea tu nueva contraseña.' });
            setTimeout(() => navigate(ROUTES.AUTH.NEW_PASSWORD), 1000);
        } catch (error) {
            handleApiError(error);
        }
    };

    const handleReenviar = async () => {
        if (cooldown > 0) return;

        try {
            const res = await requestRecoveryMutation.mutateAsync(email);
            showSuccessToast(res.successCode, res.metadata);
            setCodigo("");
            startCooldown(60);
        } catch (error) {
            handleApiError(error);
        }
    };

    if (!email) return <Navigate to={ROUTES.AUTH.LOGIN} replace />;

    return (
        <LoginContainer titulo="Autenticación requerida">
            <StepProgress steps={RECOVERY_STEPS} currentStep={1} />
            <VerificationForm
                email={email}
                codigo={codigo}
                onCodigoChange={setCodigo}
                onVerificar={handleVerificar}
                onReenviar={handleReenviar}
                cargando={verifyRecoveryMutation.isPending || requestRecoveryMutation.isPending}
                cooldown={cooldown}
            />
        </LoginContainer>
    );
};