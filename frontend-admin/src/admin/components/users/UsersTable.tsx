import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { Card } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Skeleton } from '@/components/ui/skeleton';
import { ChevronLeft, ChevronRight, Shield, ShieldOff, UserCheck, UserX } from 'lucide-react';
import type { AdminUser, UserFilters } from '@/admin/types/userTypes';
import type { ConfirmActionType } from './UserActionDialog';

interface Props {
    usuarios: AdminUser[];
    isLoading: boolean;
    isPending: boolean;
    setConfirmAction: (action: ConfirmActionType | null) => void;
    totalPages: number;
    filters: UserFilters;
    setFilters: React.Dispatch<React.SetStateAction<UserFilters>>;
}

const formatDate = (dateStr: string | null) => {
    if (!dateStr) return '—';
    return new Date(dateStr).toLocaleDateString('es-PE', {
        day: '2-digit', month: 'short', year: 'numeric',
        hour: '2-digit', minute: '2-digit'
    });
};

export const UsersTable = ({
    usuarios,
    isLoading,
    isPending,
    setConfirmAction,
    totalPages,
    filters,
    setFilters
}: Props) => {
    return (
        <Card>
            <div className="overflow-x-auto">
                <Table>
                    <TableHeader>
                        <TableRow className="bg-muted/50 hover:bg-muted/50">
                            <TableHead className="w-50">Usuario</TableHead>
                            <TableHead className="hidden md:table-cell">Proveedor</TableHead>
                            <TableHead className="text-center">Rol</TableHead>
                            <TableHead className="text-center">Estado</TableHead>
                            <TableHead className="hidden lg:table-cell">Última sesión</TableHead>
                            <TableHead className="hidden lg:table-cell">Registro</TableHead>
                            <TableHead className="text-center">Acciones</TableHead>
                        </TableRow>
                    </TableHeader>
                    <TableBody>
                        {isLoading ? (
                            Array.from({ length: 5 }).map((_, i) => (
                                <TableRow key={i}>
                                    <TableCell colSpan={7}>
                                        <Skeleton className="h-8 w-full" />
                                    </TableCell>
                                </TableRow>
                            ))
                        ) : usuarios.length === 0 ? (
                            <TableRow>
                                <TableCell colSpan={7} className="h-24 text-center text-muted-foreground">
                                    No se encontraron usuarios
                                </TableCell>
                            </TableRow>
                        ) : (
                            usuarios.map((user) => (
                                <TableRow key={user.id} className="transition-colors">
                                    <TableCell>
                                        <div className="flex flex-col">
                                            <span className="font-medium text-foreground text-sm">
                                                {user.nombre ?? ''} {user.apellido ?? ''}
                                            </span>
                                            <span className="text-xs text-muted-foreground">{user.email}</span>
                                        </div>
                                    </TableCell>
                                    <TableCell className="hidden md:table-cell">
                                        <div className="flex gap-1 flex-wrap">
                                            {user.providers.map(p => (
                                                <Badge key={p} variant="outline" className="text-xs">
                                                    {p}
                                                </Badge>
                                            ))}
                                        </div>
                                    </TableCell>
                                    <TableCell className="text-center">
                                        <Badge variant={user.rol === 'ADMIN' ? 'default' : 'secondary'} className="text-xs">
                                            {user.rol === 'ADMIN' ? '🛡️ Admin' : '👤 User'}
                                        </Badge>
                                    </TableCell>
                                    <TableCell className="text-center">
                                        <Badge variant={user.estado ? 'default' : 'destructive'} className="text-xs">
                                            {user.estado ? '✅ Activo' : '❌ Inactivo'}
                                        </Badge>
                                    </TableCell>
                                    <TableCell className="text-xs text-muted-foreground hidden lg:table-cell">
                                        {formatDate(user.ultimaSesion)}
                                    </TableCell>
                                    <TableCell className="text-xs text-muted-foreground hidden lg:table-cell">
                                        {formatDate(user.fechaRegistro)}
                                    </TableCell>
                                    <TableCell>
                                        <div className="flex items-center justify-center gap-1">
                                            <Button
                                                variant="ghost"
                                                size="sm"
                                                disabled={isPending}
                                                onClick={() => setConfirmAction({
                                                    type: 'rol',
                                                    user,
                                                    value: user.rol === 'ADMIN' ? 'USER' : 'ADMIN'
                                                })}
                                                title={user.rol === 'ADMIN' ? 'Quitar Admin' : 'Hacer Admin'}
                                            >
                                                {user.rol === 'ADMIN' ? (
                                                    <ShieldOff className="w-4 h-4 text-orange-500" />
                                                ) : (
                                                    <Shield className="w-4 h-4 text-blue-500" />
                                                )}
                                            </Button>
                                            <Button
                                                variant="ghost"
                                                size="sm"
                                                disabled={isPending}
                                                onClick={() => setConfirmAction({
                                                    type: 'estado',
                                                    user,
                                                    value: !user.estado
                                                })}
                                                title={user.estado ? 'Desactivar' : 'Activar'}
                                            >
                                                {user.estado ? (
                                                    <UserX className="w-4 h-4 text-red-500" />
                                                ) : (
                                                    <UserCheck className="w-4 h-4 text-green-500" />
                                                )}
                                            </Button>
                                        </div>
                                    </TableCell>
                                </TableRow>
                            ))
                        )}
                    </TableBody>
                </Table>
            </div>

            {totalPages > 1 && (
                <div className="flex items-center justify-between px-4 py-3 border-t">
                    <span className="text-sm text-muted-foreground">
                        Página {filters.page + 1} de {totalPages}
                    </span>
                    <div className="flex gap-2">
                        <Button
                            variant="outline"
                            size="sm"
                            disabled={filters.page === 0}
                            onClick={() => setFilters(prev => ({ ...prev, page: prev.page - 1 }))}
                        >
                            <ChevronLeft className="w-4 h-4 mr-1" /> Anterior
                        </Button>
                        <Button
                            variant="outline"
                            size="sm"
                            disabled={filters.page >= totalPages - 1}
                            onClick={() => setFilters(prev => ({ ...prev, page: prev.page + 1 }))}
                        >
                            Siguiente <ChevronRight className="w-4 h-4 ml-1" />
                        </Button>
                    </div>
                </div>
            )}
        </Card>
    );
};
