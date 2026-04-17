import { useNavigate } from 'react-router-dom';
import { ROUTES } from '@/router/routes';
import { useEstaAutenticado } from '@/auth/store/authStore';
import { UserMenu } from '@/user/components/UserMenu';
import { Button } from '@/components/ui/button';
import { ThemeToggle } from '@/components/theme/ThemeToggle';
import { ShoppingBag } from 'lucide-react';

export const LandingHeader = () => {
    const navigate = useNavigate();
    const estaAutenticado = useEstaAutenticado();

    return (
        <header className="sticky top-0 z-50 w-full border-b bg-background/95 backdrop-blur supports-backdrop-filter:bg-background/60">
            <div className="max-w-7xl mx-auto px-4 h-16 flex justify-between items-center">
                <div className="flex items-center gap-2">
                    <ShoppingBag className="h-6 w-6 text-primary" />
                    <span className="text-xl font-bold tracking-tight text-foreground">Mi Tienda</span>
                </div>
                <div className="flex items-center gap-4">
                    <ThemeToggle />
                    {estaAutenticado ? (
                        <UserMenu />
                    ) : (
                        <Button onClick={() => navigate(ROUTES.AUTH.LOGIN)} size="sm">
                            Iniciar Sesión
                        </Button>
                    )}
                </div>
            </div>
        </header>
    );
};
