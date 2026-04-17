import { useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { LandingSkeleton } from '@/tienda/components/LandingSkeleton';
import { ROUTES } from '@/router/routes';
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert';
import { AlertCircle } from 'lucide-react';
import { LoginContainer } from '../components/layout/LoginContainer';
import { validarSesionActualService } from '../services/LoginService';
import { useAuthStore } from '../store/authStore';
import { resetSessionState } from '@/core/services/apiClient';


export const OAuthCallback = () => {
    const navigate = useNavigate();
    const [searchParams] = useSearchParams();
    const login = useAuthStore((state) => state.login);

    const errorParam = searchParams.get('error');
    const authSuccess = searchParams.get('auth');
    const hayError = !!errorParam || authSuccess !== 'success';
    const mensajeError = decodeURIComponent(errorParam || 'Error en la autenticación OAuth');

    useEffect(() => {
        if (hayError) {
            const timer = setTimeout(() => {
                navigate(ROUTES.AUTH.LOGIN, { state: { error: mensajeError } });
            }, 2500);
            return () => clearTimeout(timer);
        }

        resetSessionState();
        validarSesionActualService()
            .then((datos) => {
                login({ email: datos.email, nombre: datos.nombre, rol: datos.rol });
                const channel = new BroadcastChannel('auth_channel');
                channel.postMessage('LOGIN');
                channel.close();
                navigate(ROUTES.HOME, { replace: true });
            })
            .catch(() => {
                navigate(ROUTES.AUTH.LOGIN, {
                    state: { error: 'No se pudo validar la sesión OAuth.' }
                });
            });

    }, [hayError, mensajeError, navigate, login]);

    if (hayError) {
        return (
            <LoginContainer titulo="Error de autenticación">
                <Alert variant="destructive" className="mb-6">
                    <AlertCircle className="h-4 w-4" />
                    <AlertTitle>Error</AlertTitle>
                    <AlertDescription>{mensajeError}</AlertDescription>
                </Alert>
                <p className="text-sm text-muted-foreground text-center">
                    Serás redirigido al inicio de sesión...
                </p>
            </LoginContainer>
        );
    }

    return <LandingSkeleton />;
};
