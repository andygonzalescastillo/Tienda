import { useRouteError, isRouteErrorResponse } from 'react-router-dom';
import { Button } from '@/components/ui/button';
import { Card, CardContent } from '@/components/ui/card';

export const GlobalError = () => {
    const error = useRouteError();

    let errorMessage = 'Ha ocurrido un error inesperado.';
    let errorTitle = 'Algo salió mal';

    if (isRouteErrorResponse(error)) {
        errorTitle = `${error.status} ${error.statusText}`;
        errorMessage = error.data?.message || 'Error en la navegación.';
    } else if (error instanceof Error) {
        errorMessage = error.message;
    }

    return (
        <div className="min-h-screen flex items-center justify-center bg-background p-4">
            <Card className="max-w-md w-full text-center shadow-lg">
                <CardContent className="p-8">
                    <div className="w-16 h-16 bg-destructive/10 rounded-full flex items-center justify-center mx-auto mb-4">
                        <span className="text-3xl">⚠️</span>
                    </div>
                    <h1 className="text-xl font-bold text-foreground mb-2">{errorTitle}</h1>
                    <p className="text-muted-foreground mb-6">{errorMessage}</p>
                    <div className="flex gap-3 justify-center">
                        <Button onClick={() => window.location.reload()} variant="outline">
                            Recargar página
                        </Button>
                        <Button onClick={() => window.location.href = '/'}>
                            Ir al inicio
                        </Button>
                    </div>
                </CardContent>
            </Card>
        </div>
    );
};
