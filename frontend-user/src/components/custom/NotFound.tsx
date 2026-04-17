import { Button } from '@/components/ui/button';
import { Card, CardContent } from '@/components/ui/card';

export const NotFound = () => {
    return (
        <div className="min-h-screen flex items-center justify-center bg-background p-4">
            <Card className="max-w-md w-full text-center shadow-lg">
                <CardContent className="p-8">
                    <div className="w-20 h-20 bg-muted rounded-full flex items-center justify-center mx-auto mb-5">
                        <span className="text-4xl">🔍</span>
                    </div>
                    <h1 className="text-6xl font-extrabold text-primary mb-2">404</h1>
                    <h2 className="text-xl font-semibold text-foreground mb-2">Página no encontrada</h2>
                    <p className="text-muted-foreground mb-6">
                        La página que buscas no existe o fue movida.
                    </p>
                    <Button
                        className="font-medium h-11 px-8"
                        onClick={() => window.location.href = '/'}
                    >
                        Volver al inicio
                    </Button>
                </CardContent>
            </Card>
        </div>
    );
};
