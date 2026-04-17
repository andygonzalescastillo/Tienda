import { z } from 'zod';

const REQUIRED = "Campo obligatorio";
const EMAIL = z.email({ message: "Correo inválido" });
const PASSWORD = z.string().regex(/^(?=.*[a-zA-Z])(?=.*\d).{8,}$/, "Mínimo 8 caracteres, debe contener letras y números");

export const loginSchema = z.object({ email: EMAIL, password: z.string().min(1, REQUIRED) });
export const emailOnlySchema = z.object({ email: EMAIL });
export const newPasswordSchema = z.object({ password: PASSWORD, confirmPassword: z.string() }).refine(data => data.password === data.confirmPassword, {
    message: "Las contraseñas no coinciden",
    path: ["confirmPassword"],
});

export type LoginSchemaType = z.infer<typeof loginSchema>;
export type EmailOnlySchemaType = z.infer<typeof emailOnlySchema>;
export type NewPasswordSchemaType = z.infer<typeof newPasswordSchema>;
