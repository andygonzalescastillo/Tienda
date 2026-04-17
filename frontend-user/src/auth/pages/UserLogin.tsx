import { useEffect, useRef } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { toast } from 'sonner';
import { LoginContainer } from '../components/layout/LoginContainer';
import { EmailForm } from '../components/forms/EmailForm';
import { useLoginOrchestrator } from '../hooks/useLoginOrchestrator';
import type { EmailOnlySchemaType } from '../utils/authSchemas';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { showSuccessToast } from '@/core/utils/messageMapper';
import { useAuthFlowStore } from '../store/authFlowStore';

export const UserLogin = () => {
    const location = useLocation();
    const { error = '', mensaje = null, successCode = null } = location.state || {};

    const storeEmail = useAuthFlowStore((s) => s.email);
    const storeFrom = useAuthFlowStore((s) => s.from);
    const setFlow = useAuthFlowStore((s) => s.setFlow);

    const locationEmail = location.state?.email || '';
    const locationFrom = location.state?.from || '/';

    const email = locationEmail || storeEmail;
    const from = locationFrom !== '/' ? locationFrom : storeFrom;

    useEffect(() => {
        if (locationEmail) {
            setFlow({ email: locationEmail, from: locationFrom });
        }
    }, [locationEmail, locationFrom, setFlow]);

    const { handleLoginFlow, isLoading, error: errorOrchestrator, oauthHandlers, cargandoOAuth } = useLoginOrchestrator({ emailUsuario: email, from });

    const navigate = useNavigate();
    const toastShownRef = useRef('');

    useEffect(() => {
        const currentToastId = `${successCode}-${mensaje}-${error}-${location.key}`;

        if (toastShownRef.current === currentToastId) return;

        if (successCode || mensaje) {
            if (successCode) {
                showSuccessToast(successCode);
            } else {
                toast.success(mensaje);
            }
            toastShownRef.current = currentToastId;
            navigate(location.pathname, { replace: true, state: {} });
        } else if (error) {
            toast.error(error);
            toastShownRef.current = currentToastId;
            navigate(location.pathname, { replace: true, state: {} });
        }
    }, [mensaje, error, successCode, navigate, location.pathname, location.key]);

    useEffect(() => {
        if (errorOrchestrator) {
            toast.error(errorOrchestrator);
        }
    }, [errorOrchestrator]);

    const onSubmit = async (data: EmailOnlySchemaType) => {
        await handleLoginFlow(data.email);
    };

    return (
        <LoginContainer
            titulo="Te damos la bienvenida"
            descripcion="Inicia sesión o crea una cuenta nueva para comenzar."
        >
            {from !== '/' && (
                <Alert className="mb-4 bg-primary/10 border-primary/20 text-primary">
                    <AlertDescription className="text-center font-medium">Inicia sesión para continuar</AlertDescription>
                </Alert>
            )}

            <EmailForm
                isSubmitting={isLoading}
                onSubmit={onSubmit}
                cargandoOAuth={cargandoOAuth}
                defaultEmail={email}
                {...oauthHandlers}
            />
        </LoginContainer>
    );
};