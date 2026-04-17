import { useNavigate } from 'react-router-dom';
import { ROUTES } from '@/router/routes';
import { useAdminAuthFlowStore } from '@/auth/store/adminAuthFlowStore';
import { useRequestPasswordRecovery } from '@/auth/hooks/useRecoveryMutations';
import { type EmailOnlySchemaType } from '@/auth/utils/authSchemas';
import { showSuccessToast } from '@/core/utils/messageMapper';
import { handleApiError } from '@/core/utils/handleApiError';
import { LoginContainer } from '@/auth/components/layout/LoginContainer';
import { AdminRecoveryForm } from '@/auth/components/forms/AdminRecoveryForm';
import { StepProgress } from '@/components/custom/StepProgress';
import { RECOVERY_STEPS } from '@/core/constants/authConstants';

export const AdminRecovery = () => {
    const navigate = useNavigate();
    const { recoveryEmail, setRecoveryEmail } = useAdminAuthFlowStore();

    const requestMutation = useRequestPasswordRecovery();

    const onSubmit = async (data: EmailOnlySchemaType) => {
        try {
            const res = await requestMutation.mutateAsync(data.email);
            showSuccessToast(res.successCode, res.metadata);
            setRecoveryEmail(data.email);
            navigate(ROUTES.AUTH.VERIFY_RECOVERY);
        } catch (error) {
            handleApiError(error);
        }
    };

    return (
        <LoginContainer
            titulo="Recuperar contraseña"
            descripcion="Ingresa tu correo y te enviaremos un código seguro para ayudarte a entrar."
        >
            <StepProgress steps={RECOVERY_STEPS} currentStep={0} />
            <AdminRecoveryForm
                email={recoveryEmail}
                isSubmitting={requestMutation.isPending}
                onSubmit={onSubmit}
                onVolverLogin={() => navigate(ROUTES.AUTH.LOGIN)}
            />
        </LoginContainer>
    );
};
