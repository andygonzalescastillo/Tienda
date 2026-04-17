import { useNavigate, Navigate } from 'react-router-dom';
import { toast } from 'sonner';
import { ROUTES } from '@/router/routes';
import { LoginContainer } from '../components/layout/LoginContainer';
import { NewPasswordForm } from '../components/forms/NewPasswordForm';
import { useResetPassword } from '../hooks/mutations/useRecoveryMutations';
import { handleApiError } from '@/core/utils/handleApiError';
import { StepProgress } from '@/components/custom/StepProgress';
import { RECOVERY_STEPS } from '@/core/constants/authConstants';
import type { NewPasswordSchemaType } from '../utils/authSchemas';
import { useAuthFlowStore } from '../store/authFlowStore';

export const UserNewPassword = () => {
    const navigate = useNavigate();
    const { email, codigoRecuperacion: codigo, limpiar } = useAuthFlowStore();

    const resetMutation = useResetPassword();

    const onSubmit = async (data: NewPasswordSchemaType) => {
        if (codigo.length !== 6) {
            return toast.error('Código incompleto', { description: 'El código de verificación debe tener 6 dígitos.' });
        }

        try {
            await resetMutation.mutateAsync({ email, codigo, nuevaPassword: data.password });
            limpiar();
            navigate(ROUTES.AUTH.LOGIN, {
                state: { successCode: 'PASSWORD_UPDATED' }
            });
        } catch (error) {
            handleApiError(error);
        }
    };

    if (!email || !codigo) return <Navigate to={ROUTES.AUTH.LOGIN} replace />;

    return (
        <LoginContainer titulo="Nueva contraseña">
            <StepProgress steps={RECOVERY_STEPS} currentStep={2} />
            <NewPasswordForm
                email={email}
                isSubmitting={resetMutation.isPending}
                onSubmit={onSubmit}
            />
        </LoginContainer>
    );
};