import { useNavigate, Navigate } from 'react-router-dom';
import { ROUTES } from '@/router/routes';
import { LoginContainer } from '../components/layout/LoginContainer';
import { LoginPasswordForm } from '../components/forms/LoginPasswordForm';
import { useLogin } from '../hooks/mutations/useAuthMutations';
import { handleApiError } from '@/core/utils/handleApiError';
import type { LoginPasswordSchemaType } from '../utils/authSchemas';
import { useSessionSync } from '../hooks/useSessionSync';
import { useAuthFlowStore } from '../store/authFlowStore';

export const UserLoginPassword = () => {
    const navigate = useNavigate();
    const { email, from, limpiar } = useAuthFlowStore();

    const loginMutation = useLogin();
    const { broadcastLogin } = useSessionSync();

    const onSubmit = async (data: LoginPasswordSchemaType) => {
        try {
            await loginMutation.mutateAsync({ email, password: data.password });
            broadcastLogin();
            limpiar();
            navigate(from, { replace: true });
        } catch (error) {
            handleApiError(error);
        }
    };

    if (!email) return <Navigate to={ROUTES.AUTH.LOGIN} replace />;

    return (
        <LoginContainer titulo="Introduce tu contraseña">
            <LoginPasswordForm
                email={email}
                isSubmitting={loginMutation.isPending}
                onSubmit={onSubmit}
                onRecuperarPassword={() => navigate(ROUTES.AUTH.RECOVERY)}
                onEditarEmail={() => navigate(ROUTES.AUTH.LOGIN)}
            />
        </LoginContainer>
    );
};