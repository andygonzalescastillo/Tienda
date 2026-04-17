export interface DatosAdmin {
    email: string;
    nombre: string;
    rol: string;
}

export interface LoginResponse extends DatosAdmin {
    successCode: string;
}
