export interface AdminUser {
    id: number;
    email: string;
    nombre: string | null;
    apellido: string | null;
    rol: 'USER' | 'ADMIN';
    estado: boolean;
    emailVerificado: boolean;
    providers: string[];
    ultimaSesion: string | null;
    fechaRegistro: string;
}

export interface AdminUserPage {
    content: AdminUser[];
    totalElements: number;
    totalPages: number;
    number: number;
    size: number;
    first: boolean;
    last: boolean;
}

export interface UserFilters {
    search: string;
    rol: string;
    estado: string;
    page: number;
    size: number;
}
