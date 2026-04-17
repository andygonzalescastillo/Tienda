import { useState } from 'react';
import { toast } from 'sonner';
import { useSessionsQuery } from '@/admin/hooks/useSessionsQuery';
import { useCloseSessionMutation } from '@/admin/hooks/useSessionMutations';
import { useSessionManager } from '@/admin/hooks/useSessionManager';
import { getSuccessMessage } from '@/core/utils/messageMapper';
import { ROUTES } from '@/router/routes';
import { SessionCard } from '@/admin/components/SessionCard';
import { SessionSkeleton } from '@/admin/components/SessionSkeleton';
import { EmptySessions } from '@/admin/components/EmptySessions';
import { Trash2 } from 'lucide-react';
import { AlertDialog, AlertDialogAction, AlertDialogCancel, AlertDialogContent, AlertDialogDescription, AlertDialogFooter, AlertDialogHeader, AlertDialogTitle } from "@/components/ui/alert-dialog";
import { Button } from "@/components/ui/button";
import { AdminPageHeader } from '@/components/custom/AdminPageHeader';

export const AdminSessions = () => {
    const { data: sesiones = [], isLoading } = useSessionsQuery();
    const closeMutation = useCloseSessionMutation();
    const { logout: closeAllMutation } = useSessionManager();
    const [confirm, setConfirm] = useState(false);

    const handleCloseSession = (tokenId: string) => {
        toast.promise(closeMutation.mutateAsync(tokenId), {
            loading: 'Cerrando sesión...',
            success: (data) => getSuccessMessage(data.response.successCode),
            error: (err) => err instanceof Error ? err.message : 'Error al cerrar sesión'
        });
    };

    const handleCloseAll = () => {
        setConfirm(false);
        toast.promise(closeAllMutation.mutateAsync(true), {
            loading: 'Cerrando todas las sesiones...',
            success: 'Todas las sesiones han sido cerradas',
            error: 'Error al cerrar sesiones'
        });
    };

    const cargando = closeMutation.isPending || closeAllMutation.isPending;

    const sesionActual = sesiones.filter(s => s.esActual);
    const otrasSesiones = sesiones.filter(s => !s.esActual);

    const sections = [
        { title: 'Esta sesión', items: sesionActual },
        { title: `Otras sesiones (${otrasSesiones.length})`, items: otrasSesiones }
    ];

    const tieneOtrasSesiones = sesiones.some(s => !s.esActual);

    return (
        <div className="min-h-screen bg-background flex flex-col">
            <AdminPageHeader breadcrumbs={[
                { label: 'Configuración', to: ROUTES.ADMIN.CONFIGURATION },
                { label: 'Sesiones' }
            ]} />

            <main className="flex-1 max-w-4xl w-full mx-auto px-4 py-8">
                <div className="flex flex-col sm:flex-row sm:items-end justify-between gap-5 pb-8">
                    <div className="space-y-1.5">
                        <h1 className="text-3xl sm:text-4xl font-extrabold tracking-tight text-foreground">Gestión de dispositivos</h1>
                        <p className="text-muted-foreground text-lg">Administra dónde estás conectado y cierra sesiones sospechosas.</p>
                    </div>

                    {tieneOtrasSesiones && (
                        <Button
                            variant="destructive"
                            size="default"
                            onClick={() => setConfirm(true)}
                            disabled={cargando}
                            className="w-fit font-semibold shadow-sm shrink-0"
                        >
                            <Trash2 className="w-4 h-4 mr-2" /> Cerrar todas las demás
                        </Button>
                    )}
                </div>

                <AlertDialog open={confirm} onOpenChange={setConfirm}>
                    <AlertDialogContent>
                        <AlertDialogHeader>
                            <AlertDialogTitle>¿Cerrar todas las sesiones?</AlertDialogTitle>
                            <AlertDialogDescription>
                                Esto cerrará tu sesión en todos los demás dispositivos vinculados.
                                Solo se mantendrá activa la sesión que estás usando ahora.
                            </AlertDialogDescription>
                        </AlertDialogHeader>
                        <AlertDialogFooter>
                            <AlertDialogCancel disabled={cargando}>Cancelar</AlertDialogCancel>
                            <AlertDialogAction
                                onClick={handleCloseAll}
                                className="bg-destructive text-destructive-foreground hover:bg-destructive/90"
                                disabled={cargando}
                            >
                                {cargando ? 'Cerrando...' : 'Cerrar todas'}
                            </AlertDialogAction>
                        </AlertDialogFooter>
                    </AlertDialogContent>
                </AlertDialog>

                {isLoading ? (
                    <div className="space-y-4">
                        {[1, 2, 3].map((i) => (
                            <SessionSkeleton key={i} />
                        ))}
                    </div>
                ) : sesiones.length === 0 ? (
                    <EmptySessions />
                ) : (
                    <div className="grid gap-10">
                        {sections.map(section => section.items.length > 0 && (
                            <section key={section.title} aria-labelledby={`title-${section.title.replace(/\s+/g, '-')}`}>
                                <h2 id={`title-${section.title.replace(/\s+/g, '-')}`} className="text-xl font-bold text-foreground mb-4 px-1">{section.title}</h2>
                                <div className="grid gap-4">
                                    {section.items.map(s => (
                                        <SessionCard
                                            key={s.tokenId}
                                            sesion={s}
                                            onCerrar={handleCloseSession}
                                            cargando={cargando}
                                        />
                                    ))}
                                </div>
                            </section>
                        ))}
                    </div>
                )}
            </main>
        </div>
    );
};
