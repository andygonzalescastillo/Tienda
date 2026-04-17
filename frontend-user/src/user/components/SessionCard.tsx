import type { DeviceType, SesionResponse } from '../types/sessionTypes';
import { format, formatDistanceToNow, differenceInHours, differenceInMinutes } from 'date-fns';
import { es } from 'date-fns/locale';
import { X, Clock, MapPin, Monitor, Smartphone, Tablet } from 'lucide-react';
import { Card, CardContent } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Tooltip, TooltipContent, TooltipTrigger } from "@/components/ui/tooltip";

const DISPOSITIVO_CONFIG: Record<DeviceType, { icon: typeof Monitor; label: string }> = {
    MOBILE: { icon: Smartphone, label: 'Móvil' },
    TABLET: { icon: Tablet, label: 'Tablet' },
    DESKTOP: { icon: Monitor, label: 'Escritorio' },
    UNKNOWN: { icon: Monitor, label: 'Desconocido' }
} as const;

export const SessionCard = ({ sesion, onCerrar, cargando }: { sesion: SesionResponse; onCerrar: (id: string) => void; cargando: boolean }) => {
    const { icon: Icon } = DISPOSITIVO_CONFIG[sesion.tipoDispositivo] || DISPOSITIVO_CONFIG.UNKNOWN;
    const fechaUltimoAcceso = new Date(sesion.ultimoAcceso);
    const horasDiferencia = differenceInHours(new Date(), fechaUltimoAcceso);
    const minutosDiferencia = differenceInMinutes(new Date(), fechaUltimoAcceso);

    const esActivoAhora = minutosDiferencia < 6;

    let ultimaActividad;
    if (esActivoAhora) {
        ultimaActividad = 'Activo ahora';
    } else if (horasDiferencia < 24) {
        ultimaActividad = `Activo hace ${formatDistanceToNow(fechaUltimoAcceso, { addSuffix: false, locale: es })}`;
    } else {
        ultimaActividad = `Última actividad el ${format(fechaUltimoAcceso, "d 'de' MMMM", { locale: es })}`;
    }

    return (
        <Card className={`transition-shadow hover:shadow-md ${sesion.esActual ? 'border-primary ring-1 ring-primary/20' : 'border-border'}`}>
            <CardContent className="p-5 flex items-start justify-between gap-4">
                <div className="flex gap-4 sm:gap-6 flex-1 min-w-0 items-center">
                    <div className={`w-14 h-14 rounded-2xl flex items-center justify-center shrink-0 transition-colors ${sesion.esActual ? 'bg-primary/10 text-primary shadow-inner shadow-primary/20' : 'bg-muted text-muted-foreground'}`}>
                        <Icon strokeWidth={2.5} className="w-7 h-7" />
                    </div>

                    <div className="flex-1 min-w-0 space-y-2">
                        <div className="flex flex-wrap items-center gap-2.5 mb-1">
                            <h3 className="font-bold text-base text-foreground truncate">
                                {sesion.nombreDispositivo}
                            </h3>
                            {sesion.esActual && (
                                <Badge variant="default" className="bg-primary text-primary-foreground pointer-events-none uppercase tracking-wider text-[10px] px-2 py-0.5">
                                    Este dispositivo
                                </Badge>
                            )}
                        </div>

                        <div className="flex flex-wrap gap-2 text-xs font-medium">
                            <Badge variant="outline" className="text-muted-foreground font-normal border-border/60 gap-1.5 py-1" title="Ubicación">
                                <MapPin className="w-3.5 h-3.5 text-muted-foreground/70" /> {sesion.ubicacion || 'Desconocido'}
                            </Badge>

                            <Badge variant="secondary" className={`${esActivoAhora ? 'bg-primary/10 text-primary hover:bg-primary/20' : 'bg-muted text-muted-foreground'} gap-1.5 py-1`} title={format(new Date(sesion.ultimoAcceso), "dd/MM/yyyy HH:mm", { locale: es })}>
                                <Clock className="w-3.5 h-3.5" /> {ultimaActividad}
                            </Badge>
                        </div>
                    </div>
                </div>

                {!sesion.esActual && (
                    <Tooltip>
                        <TooltipTrigger asChild>
                            <Button
                                variant="ghost"
                                size="icon"
                                onClick={() => onCerrar(sesion.tokenId)}
                                disabled={cargando}
                                className="ml-2 text-destructive hover:text-destructive hover:bg-destructive/10"
                                aria-label="Cerrar sesión"
                            >
                                <X className="w-5 h-5" />
                            </Button>
                        </TooltipTrigger>
                        <TooltipContent>
                            <p>Cerrar esta sesión</p>
                        </TooltipContent>
                    </Tooltip>
                )}
            </CardContent>
        </Card>
    );
};