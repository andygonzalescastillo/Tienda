import { useNavigate, Navigate } from 'react-router-dom';
import { ROUTES } from '@/router/routes';
import { useAdminAuthFlowStore } from '@/auth/store/adminAuthFlowStore';
import { useResetPassword } from '@/auth/hooks/useRecoveryMutations';
import { type NewPasswordSchemaType } from '@/auth/utils/authSchemas';
import { showSuccessToast } from '@/core/utils/messageMapper';
import { handleApiError } from '@/core/utils/handleApiError';
import { LoginContainer } from '@/auth/components/layout/LoginContainer';
import { AdminNewPasswordForm } from '@/auth/components/forms/AdminNewPasswordForm';
import { StepProgress } from '@/components/custom/StepProgress';
import { RECOVERY_STEPS } from '@/core/constants/authConstants';

export const AdminNewPassword = () => {
    const navigate = useNavigate();
    const { recoveryEmail, recoveryCode, clearFlow } = useAdminAuthFlowStore();

    const resetMutation = useResetPassword();

    const onSubmit = async (data: NewPasswordSchemaType) => {
        try {
            const res = await resetMutation.mutateAsync({ email: recoveryEmail, codigo: recoveryCode, nuevaPassword: data.password });
            clearFlow();
            showSuccessToast(res.successCode);
            navigate(ROUTES.AUTH.LOGIN, { replace: true });
        } catch (error) {
            handleApiError(error);
        }
    };

    if (!recoveryEmail || !recoveryCode) {
        return <Navigate to={ROUTES.AUTH.LOGIN} replace />;
    }

    return (
        <LoginContainer titulo="Nueva contraseña">
            <StepProgress steps={RECOVERY_STEPS} currentStep={2} />
            <AdminNewPasswordForm
                email={recoveryEmail}
                isSubmitting={resetMutation.isPending}
                onSubmit={onSubmit}
            />
        </LoginContainer>
    );
};
