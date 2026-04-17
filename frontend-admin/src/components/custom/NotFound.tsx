import { useNavigate } from 'react-router-dom';
import { Button } from '@/components/ui/button';
import { Card, CardContent } from '@/components/ui/card';
import { ROUTES } from '@/router/routes';

export const NotFound = () => {
    const navigate = useNavigate();

    return (
        <div className="min-h-screen flex items-center justify-center bg-background p-4">
            <Card className="max-w-md w-full text-center shadow-lg">
                <CardContent className="p-8">
                    <div className="w-16 h-16 bg-muted rounded-full flex items-center justify-center mx-auto mb-4">
                        <span className="text-3xl">🔍</span>
                    </div>
                    <h1 className="text-xl font-bold text-foreground mb-2">Página no encontrada</h1>
                    <p className="text-muted-foreground mb-6">
                        La página que buscas no existe o fue movida.
                    </p>
                    <Button onClick={() => navigate(ROUTES.ADMIN.DASHBOARD, { replace: true })}>
                        Volver al Dashboard
                    </Button>
                </CardContent>
            </Card>
        </div>
    );
};
