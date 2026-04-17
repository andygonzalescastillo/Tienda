export interface DatosUsuario {
    email: string;
    nombre: string;
    rol: string;
}

export interface RegisterResponse {
    email: string;
    nombre: string;
    metadata?: Record<string, unknown>;
}

export interface VerificarEmailResponse {
    existe: boolean;
    tienePassword: boolean;
    proveedor: string | null;
    proveedoresVinculados: string[] | null;
}

export interface LoginResponse extends DatosUsuario {
    successCode: string;
}
