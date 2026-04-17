import { z } from 'zod';

const envSchema = z.object({
    VITE_API_URL: z.string().default(""),
    VITE_WS_URL: z.string({ message: "La URL del WebSocket debe ser válida" }),
    VITE_BACKEND_URL: z.string().default(""),
});

export const env = envSchema.parse(import.meta.env);