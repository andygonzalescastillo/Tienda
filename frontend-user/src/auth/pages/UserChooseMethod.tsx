import { useEffect } from 'react';
import { useNavigate, Navigate } from 'react-router-dom';
import { toast } from 'sonner';
import { ChooseMethodForm } from '../components/forms/ChooseMethodForm';
import { useOAuthProviders } from '../hooks/useOAuthProviders';
import { LoginContainer } from '../components/layout/LoginContainer';
import { useLogin } from '../hooks/mutations/useAuthMutations';
import { ROUTES } from '@/router/routes';
import { handleApiError } from '@/core/utils/handleApiError';
import type { LoginPasswordSchemaType } from '../utils/authSchemas';
import { useAuthFlowStore } from '../store/authFlowStore';

export const UserChooseMethod = () => {
    const navigate = useNavigate();
    const { email, from, proveedoresVinculados, limpiar } = useAuthFlowStore();

    const loginMutation = useLogin();
    const oauth = useOAuthProviders({ email });

    const onSubmit = async (data: LoginPasswordSchemaType) => {
        try {
            await loginMutation.mutateAsync({ email, password: data.password });
            limpiar();
            navigate(from, { replace: true });
        } catch (error) {
            handleApiError(error);
        }
    };

    useEffect(() => {
        const error = oauth.error || loginMutation.error;
        if (error) {
            const message = typeof error === 'string' ? error : undefined;
            if (message) toast.error(message);
        }
    }, [oauth.error, loginMutation.error]);

    if (!email) return <Navigate to={ROUTES.AUTH.LOGIN} replace />;

    return (
        <LoginContainer titulo="Elige cómo entrar">
            <ChooseMethodForm
                email={email}
                proveedoresVinculados={proveedoresVinculados}
                isSubmitting={loginMutation.isPending}
                onSubmit={onSubmit}
                onRecuperarPassword={() => navigate(ROUTES.AUTH.RECOVERY)}
                oauthHandlers={{
                    onGoogleClick: oauth.handleGoogleLogin,
                    onMicrosoftClick: oauth.handleMicrosoftLogin,
                    onFacebookClick: oauth.handleFacebookLogin,
                    onGithubClick: oauth.handleGitHubLogin
                }}
                cargandoOAuth={oauth.cargando}
            />
        </LoginContainer>
    );
};