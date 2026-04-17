import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { ROUTES } from '@/router/routes';
import { AUTH_PROVIDERS, SESSION_ERRORS } from '@/core/constants/authConstants';
import { useCheckEmail } from './mutations/useAuthMutations';
import { useOAuthProviders } from './useOAuthProviders';
import { ApiError } from '@/core/types/apiError';
import { getErrorMessage } from '@/core/utils/messageMapper';
import { useAuthFlowStore } from '../store/authFlowStore';

const isOAuthProvider = (provider: string | null | undefined): provider is keyof typeof AUTH_PROVIDERS => {
    if (!provider) return false;
    return Object.values(AUTH_PROVIDERS).includes(provider as typeof AUTH_PROVIDERS[keyof typeof AUTH_PROVIDERS]);
};

interface UseLoginOrchestratorProps {
    emailUsuario: string;
    from: string;
}

export const useLoginOrchestrator = ({ emailUsuario, from }: UseLoginOrchestratorProps) => {
    const navigate = useNavigate();
    const checkEmailMutation = useCheckEmail();
    const { setFlow, setProveedores } = useAuthFlowStore();

    const oauth = useOAuthProviders({ email: emailUsuario });

    const [orquestadorError, setOrquestadorError] = useState<string | null>(null);

    const handleLoginFlow = async (email: string) => {
        setOrquestadorError(null);
        oauth.setError(null);
        oauth.setCargando(false);

        setFlow({ email, from });

        try {
            const res = await checkEmailMutation.mutateAsync(email);

            if (!res.existe) {
                return navigate(ROUTES.AUTH.REGISTER);
            }

            if ((res.proveedoresVinculados?.length ?? 0) > 1) {
                setProveedores(res.proveedoresVinculados ?? null);
                return navigate(ROUTES.AUTH.CHOOSE_METHOD);
            }

            if (res.proveedor === AUTH_PROVIDERS.LOCAL && res.tienePassword) {
                return navigate(ROUTES.AUTH.LOGIN_PASSWORD);
            }

            if (isOAuthProvider(res.proveedor) && res.proveedor !== AUTH_PROVIDERS.LOCAL) {
                oauth.setCargando(true);
                const strategies: Record<string, () => void> = {
                    [AUTH_PROVIDERS.GOOGLE]: () => oauth.handleGoogleLogin(email),
                    [AUTH_PROVIDERS.MICROSOFT]: () => oauth.handleMicrosoftLogin(email),
                    [AUTH_PROVIDERS.FACEBOOK]: () => oauth.handleFacebookLogin(),
                    [AUTH_PROVIDERS.GITHUB]: () => oauth.handleGitHubLogin(email)
                };
                strategies[res.proveedor]?.();
            }

        } catch (err: unknown) {
            const error = err as ApiError;

            const isRevokedError =
                error?.errorCode === SESSION_ERRORS.TOKEN_REVOKED ||
                error?.errorCode === SESSION_ERRORS.SESSION_REVOKED;

            if (isRevokedError) {
                setOrquestadorError(getErrorMessage(SESSION_ERRORS.SESSION_REVOKED));
            } else {
                setOrquestadorError(getErrorMessage(error?.errorCode, error?.metadata));
            }
        }
    };

    return {
        handleLoginFlow,
        isLoading: checkEmailMutation.isPending || oauth.cargando,
        error: orquestadorError || oauth.error,
        oauthHandlers: {
            onGoogleClick: oauth.handleGoogleLogin,
            onMicrosoftClick: oauth.handleMicrosoftLogin,
            onFacebookClick: oauth.handleFacebookLogin,
            onGithubClick: oauth.handleGitHubLogin
        },
        cargandoOAuth: oauth.cargando
    };
};