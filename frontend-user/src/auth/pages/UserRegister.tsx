import { useNavigate, Navigate } from 'react-router-dom';

import { ROUTES } from '@/router/routes';
import { showSuccessToast } from '@/core/utils/messageMapper';
import { handleApiError } from '@/core/utils/handleApiError';
import { LoginContainer } from '../components/layout/LoginContainer';
import { RegisterForm } from '../components/forms/RegisterForm';
import { useRegister } from '../hooks/mutations/useAuthMutations';
import { useAuthFlowStore } from '../store/authFlowStore';

import type { RegisterSchemaType } from '../utils/authSchemas';

export const UserRegister = () => {
    const navigate = useNavigate();
    const { email } = useAuthFlowStore();

    const registerMutation = useRegister();

    const onSubmit = async (data: RegisterSchemaType) => {
        try {
            const res = await registerMutation.mutateAsync(data);
            showSuccessToast("REGISTER_SUCCESS", res.metadata);
            setTimeout(() =>
                navigate(ROUTES.AUTH.VERIFY_EMAIL, { replace: true }),
                1500
            );
        } catch (error) {
            handleApiError(error);
        }
    };

    if (!email) return <Navigate to={ROUTES.AUTH.LOGIN} replace />;

    return (
        <LoginContainer
            titulo="Crea tu cuenta"
            descripcion="Únete a nosotros y comienza a disfrutar de todos los beneficios."
        >
            <RegisterForm
                isSubmitting={registerMutation.isPending}
                onSubmit={onSubmit}
                defaultEmail={email}
            />
        </LoginContainer>
    );
};