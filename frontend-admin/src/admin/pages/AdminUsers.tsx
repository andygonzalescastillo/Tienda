import { useState, useEffect } from 'react';
import { useUsersQuery } from '@/admin/hooks/useUsersQuery';
import { useChangeRolMutation, useChangeEstadoMutation } from '@/admin/hooks/useUserMutations';
import { showSuccessToast } from '@/core/utils/messageMapper';
import { handleApiError } from '@/core/utils/handleApiError';
import type { UserFilters } from '@/admin/types/userTypes';
import { AdminPageHeader } from '@/components/custom/AdminPageHeader';
import { Users } from 'lucide-react';
import { UsersFiltersCard } from '@/admin/components/users/UsersFiltersCard';
import { UsersTable } from '@/admin/components/users/UsersTable';
import { UserActionDialog, type ConfirmActionType } from '@/admin/components/users/UserActionDialog';

const PAGE_SIZE = 10;

export const AdminUsers = () => {
    const [filters, setFilters] = useState<UserFilters>({
        search: '', rol: '', estado: '', page: 0, size: PAGE_SIZE,
    });
    const [searchInput, setSearchInput] = useState('');
    const [confirmAction, setConfirmAction] = useState<ConfirmActionType | null>(null);

    const { data, isLoading } = useUsersQuery(filters);
    const changeRol = useChangeRolMutation();
    const changeEstado = useChangeEstadoMutation();

    const isPending = changeRol.isPending || changeEstado.isPending;

    useEffect(() => {
        const timer = setTimeout(() => {
            setFilters(prev => {
                const trimmed = searchInput.trim();
                if (prev.search === trimmed) return prev;
                return { ...prev, search: trimmed, page: 0 };
            });
        }, 400);
        return () => clearTimeout(timer);
    }, [searchInput]);

    const handleFilterChange = (key: 'rol' | 'estado', value: string) => {
        setFilters(prev => ({ ...prev, [key]: value, page: 0 }));
    };

    const handleConfirm = async () => {
        if (!confirmAction) return;
        const { type, user, value } = confirmAction;

        try {
            if (type === 'rol') {
                const res = await changeRol.mutateAsync({
                    userId: user.id, rol: value as 'USER' | 'ADMIN'
                });
                showSuccessToast(res.successCode);
            } else {
                const res = await changeEstado.mutateAsync({
                    userId: user.id, estado: value as boolean
                });
                showSuccessToast(res.successCode);
            }
        } catch (error) {
            handleApiError(error);
        }
        setConfirmAction(null);
    };

    const usuarios = data?.content ?? [];
    const totalPages = data?.totalPages ?? 0;
    const totalElements = data?.totalElements ?? 0;

    return (
        <div className="min-h-screen bg-background flex flex-col">
            <AdminPageHeader breadcrumbs={[{ label: 'Usuarios' }]} />

            <main className="flex-1 max-w-6xl w-full mx-auto px-4 py-8">
                <div className="space-y-1.5 pb-6">
                    <h1 className="text-3xl sm:text-4xl font-extrabold tracking-tight text-foreground flex items-center gap-3">
                        <Users className="w-8 h-8 text-primary" />
                        Gestión de Usuarios
                    </h1>
                    <p className="text-muted-foreground text-lg">
                        {totalElements} usuario{totalElements !== 1 ? 's' : ''} registrado{totalElements !== 1 ? 's' : ''}
                    </p>
                </div>

                <UsersFiltersCard
                    filters={filters}
                    searchInput={searchInput}
                    setSearchInput={setSearchInput}
                    handleFilterChange={handleFilterChange}
                />

                <UsersTable
                    usuarios={usuarios}
                    isLoading={isLoading}
                    isPending={isPending}
                    setConfirmAction={setConfirmAction}
                    totalPages={totalPages}
                    filters={filters}
                    setFilters={setFilters}
                />
            </main>

            <UserActionDialog
                confirmAction={confirmAction}
                setConfirmAction={setConfirmAction}
                handleConfirm={handleConfirm}
                isPending={isPending}
            />
        </div>
    );
};
