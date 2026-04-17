import { z } from 'zod';

const REQUIRED = "Campo obligatorio";
const MAX_NAME = "Máximo 50 caracteres";
const EMAIL = z.email({ message: "Correo inválido" });
const PASSWORD = z.string().regex(/^(?=.*[a-zA-Z])(?=.*\d).{8,}$/, "Mínimo 8 caracteres, debe contener letras y números");
const NOMBRE = z.string().trim().min(1, REQUIRED).max(50, MAX_NAME);

export const emailOnlySchema = z.object({ email: EMAIL });
export const loginPasswordSchema = z.object({ password: z.string().min(1, REQUIRED) });

export const registerSchema = z.object({
    nombre: NOMBRE,
    apellido: NOMBRE,
    email: EMAIL,
    password: PASSWORD,
});

export const newPasswordSchema = z.object({ password: PASSWORD, confirmPassword: z.string().min(1, REQUIRED) }).refine(data => data.password === data.confirmPassword, {
    message: "Las contraseñas no coinciden",
    path: ["confirmPassword"],
});

export type RegisterSchemaType = z.infer<typeof registerSchema>;
export type EmailOnlySchemaType = z.infer<typeof emailOnlySchema>;
export type LoginPasswordSchemaType = z.infer<typeof loginPasswordSchema>;
export type RecoverySchemaType = EmailOnlySchemaType;
export type NewPasswordSchemaType = z.infer<typeof newPasswordSchema>;