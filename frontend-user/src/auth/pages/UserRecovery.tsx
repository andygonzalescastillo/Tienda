import { useNavigate, Navigate } from 'react-router-dom';
import { ROUTES } from '@/router/routes';
import { LoginContainer } from '../components/layout/LoginContainer';
import { RecoveryForm } from '../components/forms/RecoveryForm';
import { useRequestPasswordRecovery } from '../hooks/mutations/useRecoveryMutations';
import { showSuccessToast } from '@/core/utils/messageMapper';
import { handleApiError } from '@/core/utils/handleApiError';
import { StepProgress } from '@/components/custom/StepProgress';
import { RECOVERY_STEPS } from '@/core/constants/authConstants';
import type { RecoverySchemaType } from '../utils/authSchemas';
import { useAuthFlowStore } from '../store/authFlowStore';

export const UserRecovery = () => {
    const navigate = useNavigate();
    const { email, setEmail } = useAuthFlowStore();

    const requestMutation = useRequestPasswordRecovery();

    const onSubmit = async (data: RecoverySchemaType) => {
        try {
            const res = await requestMutation.mutateAsync(data.email);
            setEmail(data.email);
            showSuccessToast(res.successCode, res.metadata);
            navigate(ROUTES.AUTH.VERIFY_RECOVERY);
        } catch (error) {
            handleApiError(error);
        }
    };

    if (!email) {
        return <Navigate to={ROUTES.AUTH.LOGIN} replace />;
    }

    return (
        <LoginContainer
            titulo="Recuperar contraseña"
            descripcion="Ingresa tu correo y te enviaremos un código seguro para ayudarte a entrar."
        >
            <StepProgress steps={RECOVERY_STEPS} currentStep={0} />
            <RecoveryForm
                email={email}
                isSubmitting={requestMutation.isPending}
                onSubmit={onSubmit}
                onVolverLogin={() => navigate(ROUTES.AUTH.LOGIN)}
            />
        </LoginContainer>
    );
};