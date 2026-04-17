import { useNavigate } from 'react-router-dom';
import { useUsuario } from '@/auth/store/authStore';
import { ROUTES } from '@/router/routes';
import { useSessionManager } from '@/admin/hooks/useSessionManager';
import { Settings, LogOut, Monitor } from 'lucide-react';
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuLabel, DropdownMenuSeparator, DropdownMenuTrigger } from '@/components/ui/dropdown-menu';
import { Avatar, AvatarFallback } from '@/components/ui/avatar';
import { Button } from '@/components/ui/button';

const GRADIENTS = [
    'from-blue-500 to-cyan-400',
    'from-violet-500 to-purple-400',
    'from-pink-500 to-rose-400',
    'from-amber-500 to-orange-400',
    'from-emerald-500 to-teal-400',
    'from-indigo-500 to-blue-400',
];

const getGradient = (name: string) => {
    const index = name.split('').reduce((acc, c) => acc + c.charCodeAt(0), 0) % GRADIENTS.length;
    return GRADIENTS[index];
};

export const AdminMenu = () => {
    const navigate = useNavigate();
    const usuario = useUsuario();
    const { logout } = useSessionManager();

    const iniciales = usuario?.nombre?.split(' ').map(n => n[0]).join('').toUpperCase().slice(0, 2) || 'A';
    const gradiente = getGradient(usuario?.nombre || 'A');

    const handleAction = async (action: 'config' | 'logout') => {
        if (action === 'config') return navigate(ROUTES.ADMIN.CONFIGURATION);
        await logout.mutateAsync(false);
        navigate(ROUTES.AUTH.LOGIN, { replace: true });
    };

    return (
        <DropdownMenu>
            <DropdownMenuTrigger asChild>
                <Button variant="ghost" className="relative h-10 w-10 rounded-full p-0" disabled={logout.isPending}>
                    <Avatar className="h-10 w-10">
                        <AvatarFallback className={`bg-linear-to-br ${gradiente} text-white font-bold text-sm`}>
                            {logout.isPending ? (<div className="animate-spin rounded-full h-5 w-5 border-b-2 border-white" />) : (iniciales)}
                        </AvatarFallback>
                    </Avatar>
                </Button>
            </DropdownMenuTrigger>

            <DropdownMenuContent align="end" className="w-64">
                <DropdownMenuLabel className="font-normal p-3">
                    <div className="flex items-center gap-3">
                        <Avatar className="h-9 w-9 shrink-0">
                            <AvatarFallback className={`bg-linear-to-br ${gradiente} text-white font-bold text-xs`}>
                                {iniciales}
                            </AvatarFallback>
                        </Avatar>
                        <div className="flex flex-col min-w-0">
                            <p className="text-sm font-semibold leading-tight truncate">{usuario?.nombre}</p>
                            <p className="text-xs leading-tight text-muted-foreground truncate">{usuario?.email}</p>
                        </div>
                    </div>
                </DropdownMenuLabel>

                <DropdownMenuSeparator />

                <DropdownMenuItem onClick={() => handleAction('config')}>
                    <Settings className="mr-2 h-4 w-4" />
                    <span>Configuración</span>
                </DropdownMenuItem>

                <DropdownMenuItem onClick={() => navigate(ROUTES.ADMIN.SESSIONS)}>
                    <Monitor className="mr-2 h-4 w-4" />
                    <span>Sesiones activas</span>
                </DropdownMenuItem>

                <DropdownMenuSeparator />

                <DropdownMenuItem variant="destructive" onClick={() => handleAction('logout')}>
                    <LogOut className="mr-2 h-4 w-4" />
                    <span>Cerrar Sesión</span>
                </DropdownMenuItem>
            </DropdownMenuContent>
        </DropdownMenu>
    );
};
