import { AlertDialog, AlertDialogAction, AlertDialogCancel, AlertDialogContent, AlertDialogDescription, AlertDialogFooter, AlertDialogHeader, AlertDialogTitle } from '@/components/ui/alert-dialog';
import type { AdminUser } from '@/admin/types/userTypes';

export type ConfirmActionType = {
    type: 'rol' | 'estado';
    user: AdminUser;
    value: string | boolean;
};

interface Props {
    confirmAction: ConfirmActionType | null;
    setConfirmAction: (action: ConfirmActionType | null) => void;
    handleConfirm: () => void;
    isPending: boolean;
}

export const UserActionDialog = ({
    confirmAction,
    setConfirmAction,
    handleConfirm,
    isPending
}: Props) => {
    return (
        <AlertDialog open={!!confirmAction} onOpenChange={(open) => !open && setConfirmAction(null)}>
            <AlertDialogContent>
                <AlertDialogHeader>
                    <AlertDialogTitle>
                        {confirmAction?.type === 'rol'
                            ? `¿Cambiar rol a ${confirmAction.value}?`
                            : confirmAction?.value ? '¿Activar usuario?' : '¿Desactivar usuario?'
                        }
                    </AlertDialogTitle>
                    <AlertDialogDescription>
                        {confirmAction?.type === 'rol' ? (
                            <>
                                El usuario <strong>{confirmAction.user.email}</strong> pasará de{' '}
                                <strong>{confirmAction.user.rol}</strong> a{' '}
                                <strong>{String(confirmAction.value)}</strong>.
                            </>
                        ) : confirmAction?.value ? (
                            <>
                                El usuario <strong>{confirmAction?.user.email}</strong> podrá iniciar sesión nuevamente.
                            </>
                        ) : (
                            <>
                                El usuario <strong>{confirmAction?.user.email}</strong> no podrá iniciar sesión hasta que se reactive su cuenta.
                            </>
                        )}
                    </AlertDialogDescription>
                </AlertDialogHeader>
                <AlertDialogFooter>
                    <AlertDialogCancel disabled={isPending}>Cancelar</AlertDialogCancel>
                    <AlertDialogAction
                        onClick={handleConfirm}
                        disabled={isPending}
                        className={confirmAction?.type === 'estado' && !confirmAction.value
                            ? 'bg-destructive text-destructive-foreground hover:bg-destructive/90'
                            : ''
                        }
                    >
                        {isPending ? 'Procesando...' : 'Confirmar'}
                    </AlertDialogAction>
                </AlertDialogFooter>
            </AlertDialogContent>
        </AlertDialog>
    );
};
