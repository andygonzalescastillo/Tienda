import { Monitor } from 'lucide-react';

export const EmptySessions = () => (
    <div className="text-center py-20 bg-muted/20 rounded-2xl border-2 border-dashed border-muted">
        <div className="w-16 h-16 bg-muted rounded-full flex items-center justify-center mx-auto mb-4">
            <Monitor className="w-8 h-8 text-muted-foreground" />
        </div>
        <h3 className="text-lg font-semibold text-foreground">No hay otras sesiones</h3>
        <p className="text-muted-foreground max-w-xs mx-auto">
            Todos tus dispositivos están sincronizados. Solo tienes esta sesión activa.
        </p>
    </div>
);
