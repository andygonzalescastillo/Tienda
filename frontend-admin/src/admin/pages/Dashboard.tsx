import { useNavigate } from 'react-router-dom';
import { useAuthStore } from '@/auth/store/authStore';
import { ROUTES } from '@/router/routes';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { AdminPageHeader } from '@/components/custom/AdminPageHeader';
import { Users, ChevronRight } from 'lucide-react';

export const Dashboard = () => {
    const navigate = useNavigate();
    const usuario = useAuthStore((s) => s.usuario);

    return (
        <div className="min-h-screen bg-background flex flex-col">
            <AdminPageHeader breadcrumbs={[]} />

            <main className="flex-1 max-w-5xl w-full mx-auto px-4 py-8">
                <div className="grid gap-6">
                    <Card>
                        <CardHeader>
                            <CardTitle>Bienvenido, {usuario?.nombre}</CardTitle>
                            <CardDescription>Panel de administración de la tienda</CardDescription>
                        </CardHeader>
                        <CardContent>
                            <p className="text-muted-foreground">
                                Aquí podrás gestionar los productos, pedidos, usuarios y configuración de la tienda.
                            </p>
                        </CardContent>
                    </Card>

                    <Card className="overflow-hidden hover:shadow-md transition-shadow">
                        <Button
                            variant="ghost"
                            onClick={() => navigate(ROUTES.ADMIN.USERS)}
                            className="w-full h-auto px-6 py-6 flex items-center justify-between transition-all group rounded-none"
                        >
                            <div className="flex items-center gap-5">
                                <div className="w-14 h-14 bg-primary/10 rounded-2xl flex items-center justify-center group-hover:scale-110 group-hover:bg-primary group-hover:text-primary-foreground group-hover:shadow-lg group-hover:shadow-primary/20 transition-all duration-300">
                                    <Users className="w-7 h-7 text-primary group-hover:text-primary-foreground transition-colors" />
                                </div>
                                <div className="text-left space-y-1.5">
                                    <div className="font-bold text-foreground text-lg group-hover:text-primary transition-colors">Gestión de Usuarios</div>
                                    <div className="text-sm text-muted-foreground font-medium">Busca, filtra y administra roles y estados de los usuarios</div>
                                </div>
                            </div>
                            <ChevronRight className="w-6 h-6 text-muted-foreground group-hover:translate-x-1.5 group-hover:text-primary transition-all" />
                        </Button>
                    </Card>
                </div>
            </main>
        </div>
    );
};
