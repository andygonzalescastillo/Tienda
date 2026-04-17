import { useNavigate } from 'react-router-dom';
import { ROUTES } from '@/router/routes';
import { useAdminLogin } from '@/auth/hooks/useAdminLogin';
import { useAuthStore } from '@/auth/store/authStore';
import { type LoginSchemaType } from '@/auth/utils/authSchemas';
import { handleApiError } from '@/core/utils/handleApiError';
import { showSuccessToast, showErrorToast } from '@/core/utils/messageMapper';
import { LoginContainer } from '@/auth/components/layout/LoginContainer';
import { AdminLoginForm } from '@/auth/components/forms/AdminLoginForm';
import { useAdminAuthFlowStore } from '@/auth/store/adminAuthFlowStore';

export const AdminLogin = () => {
    const navigate = useNavigate();
    const loginMutation = useAdminLogin();
    const login = useAuthStore((s) => s.login);
    const setRecoveryEmail = useAdminAuthFlowStore((s) => s.setRecoveryEmail);

    const onSubmit = async (data: LoginSchemaType) => {
        try {
            const res = await loginMutation.mutateAsync(data);
            if (res.rol !== 'ADMIN') {
                showErrorToast('ACCESS_DENIED');
                return;
            }
            login({ email: res.email, nombre: res.nombre, rol: res.rol });
            showSuccessToast(res.successCode);
            navigate(ROUTES.ADMIN.DASHBOARD, { replace: true });
        } catch (error) {
            handleApiError(error);
        }
    };

    return (
        <LoginContainer
            titulo="Panel de Administración"
            descripcion="Ingresa tus credenciales de administrador"
        >
            <AdminLoginForm
                isSubmitting={loginMutation.isPending}
                onSubmit={onSubmit}
                onRecuperarPassword={(emailStr) => {
                    setRecoveryEmail(emailStr);
                    navigate(ROUTES.AUTH.RECOVERY);
                }}
            />
        </LoginContainer>
    );
};
