import { useNavigate } from 'react-router-dom';
import { ROUTES } from '@/router/routes';
import { ChevronRight, ShieldCheck } from 'lucide-react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from "@/components/ui/button";
import { useUsuario } from '@/auth/store/authStore';
import { UserPageHeader } from '@/components/custom/UserPageHeader';

export const UserConfiguration = () => {
    const navigate = useNavigate();
    const usuario = useUsuario();

    return (
        <div className="min-h-screen bg-background flex flex-col">
            <UserPageHeader breadcrumbs={[{ label: 'Configuración' }]} />

            <main className="flex-1 max-w-4xl w-full mx-auto px-4 py-8 space-y-8">
                <div className="space-y-1.5 pb-2">
                    <h1 className="text-3xl sm:text-4xl font-extrabold tracking-tight text-foreground">Configuración de cuenta</h1>
                    <p className="text-muted-foreground text-lg">Administra tu información personal y opciones de seguridad.</p>
                </div>
                <section aria-labelledby="personal-info-title">
                    <Card className="border-border shadow-sm">
                        <CardHeader className="pb-4">
                            <CardTitle id="personal-info-title" className="text-xl font-bold flex items-center gap-2">
                                Información Personal
                            </CardTitle>
                        </CardHeader>
                        <CardContent className="grid gap-8 sm:grid-cols-2">
                            <div className="space-y-1.5 focus-within:relative">
                                <span className="text-xs font-bold text-muted-foreground uppercase tracking-widest pl-1">Nombre completo</span>
                                <div className="text-lg font-medium text-foreground bg-primary/5 px-4 py-3.5 rounded-xl border border-primary/10">
                                    {usuario?.nombre || 'No especificado'}
                                </div>
                            </div>

                            <div className="space-y-1.5 focus-within:relative">
                                <span className="text-xs font-bold text-muted-foreground uppercase tracking-widest pl-1">Correo electrónico</span>
                                <div className="text-lg font-medium text-foreground bg-primary/5 px-4 py-3.5 rounded-xl border border-primary/10">
                                    {usuario?.email}
                                </div>
                            </div>
                        </CardContent>
                    </Card>
                </section>

                <section aria-labelledby="security-title">
                    <Card className="overflow-hidden border-border bg-card hover:shadow-md transition-shadow">
                        <CardHeader className="border-b bg-muted/30 py-4">
                            <CardTitle id="security-title" className="text-lg">Seguridad</CardTitle>
                        </CardHeader>
                        <div className="p-0">
                            <Button
                                variant="ghost"
                                onClick={() => navigate(ROUTES.USER.SESSIONS)}
                                className="w-full h-auto px-6 py-6 flex items-center justify-between transition-all group rounded-none hover:bg-primary/5"
                            >
                                <div className="flex items-center gap-5">
                                    <div className="w-14 h-14 bg-primary/10 rounded-2xl flex items-center justify-center group-hover:scale-110 group-hover:bg-primary group-hover:text-primary-foreground group-hover:shadow-lg group-hover:shadow-primary/20 transition-all duration-300">
                                        <ShieldCheck className="w-7 h-7 text-primary group-hover:text-primary-foreground transition-colors" />
                                    </div>
                                    <div className="text-left space-y-1.5">
                                        <div className="font-bold text-foreground text-lg group-hover:text-primary transition-colors">Sesiones Activas</div>
                                        <div className="text-sm text-muted-foreground font-medium">Gestiona tus dispositivos y cierra sesiones remotamente</div>
                                    </div>
                                </div>
                                <ChevronRight className="w-6 h-6 text-muted-foreground group-hover:translate-x-1.5 group-hover:text-primary transition-all" />
                            </Button>
                        </div>
                    </Card>
                </section>
            </main>
        </div>
    );
};