export type DeviceType = 'MOBILE' | 'TABLET' | 'DESKTOP' | 'UNKNOWN';

export interface SesionResponse {
    id: number;
    tokenId: string;
    usuarioId: number;
    fechaCreacion: string;
    fechaExpiracion: string;
    ipAddress: string;
    userAgent: string;
    ubicacion: string;
    revocado: boolean;
    esActual: boolean;
    nombreDispositivo: string;
    tipoDispositivo: DeviceType;
    ultimoAcceso: string;
}
