import { useState } from 'react';
import { useNavigate, Navigate } from 'react-router-dom';
import { toast } from 'sonner';
import { ROUTES } from '@/router/routes';
import { LoginContainer } from '../components/layout/LoginContainer';
import { VerificationForm } from '../components/forms/VerificationForm';
import { useVerifyEmailCode, useResendEmailCode } from '../hooks/mutations/useVerificationMutations';
import { useResendCooldown } from '../hooks/useResendCooldown';
import { handleApiError } from '@/core/utils/handleApiError';
import { showSuccessToast } from '@/core/utils/messageMapper';
import { useAuthFlowStore } from '../store/authFlowStore';

export const UserVerification = () => {
    const navigate = useNavigate();
    const { email, from, limpiar } = useAuthFlowStore();

    const verifyMutation = useVerifyEmailCode();
    const resendMutation = useResendEmailCode();
    const [codigo, setCodigo] = useState("");
    const { cooldown, startCooldown } = useResendCooldown();

    const handleVerificar = async () => {
        if (codigo.length !== 6) {
            return toast.error('Código incompleto', { description: 'Ingresa los 6 dígitos del código enviado a tu correo.' });
        }

        try {
            await verifyMutation.mutateAsync({ email, codigo });
            toast.success('Email verificado', { description: '¡Tu cuenta está lista!' });
            const channel = new BroadcastChannel('auth_channel');
            channel.postMessage('LOGIN');
            channel.close();
            limpiar();
            navigate(from, { replace: true });
        } catch (error) {
            handleApiError(error);
        }
    };

    const handleReenviar = async () => {
        if (cooldown > 0) return;

        try {
            const res = await resendMutation.mutateAsync(email);
            showSuccessToast(res.successCode, res.metadata);
            setCodigo("");
            startCooldown(60);
        } catch (error) {
            handleApiError(error);
        }
    };

    if (!email) return <Navigate to={ROUTES.AUTH.LOGIN} replace />;

    return (
        <LoginContainer titulo="Casi terminamos">
            <VerificationForm
                email={email}
                codigo={codigo}
                onCodigoChange={setCodigo}
                onVerificar={handleVerificar}
                onReenviar={handleReenviar}
                cargando={verifyMutation.isPending || resendMutation.isPending}
                cooldown={cooldown}
            />
        </LoginContainer>
    );
};