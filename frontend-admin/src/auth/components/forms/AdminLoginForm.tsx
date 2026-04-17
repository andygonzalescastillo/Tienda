import { useForm, Controller } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Field, FieldLabel, FieldError } from '@/components/ui/field';
import { PasswordInput } from '@/components/ui/password-input';
import { loginSchema, type LoginSchemaType } from '../../utils/authSchemas';

interface Props {
    isSubmitting: boolean;
    onSubmit: (data: LoginSchemaType) => void;
    onRecuperarPassword: (email: string) => void;
}

export const AdminLoginForm = ({ isSubmitting, onSubmit, onRecuperarPassword }: Props) => {
    const form = useForm<LoginSchemaType>({
        resolver: zodResolver(loginSchema),
        defaultValues: { email: '', password: '' },
    });

    return (
        <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
            <Controller
                name="email"
                control={form.control}
                render={({ field, fieldState }) => (
                    <Field data-invalid={fieldState.invalid}>
                        <FieldLabel htmlFor={field.name} className="text-foreground">Correo electrónico</FieldLabel>
                        <Input
                            id={field.name}
                            type="email"
                            placeholder="admin@tienda.com"
                            autoComplete="email"
                            aria-invalid={fieldState.invalid}
                            {...field}
                        />
                        {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                    </Field>
                )}
            />

            <Controller
                name="password"
                control={form.control}
                render={({ field, fieldState }) => (
                    <Field data-invalid={fieldState.invalid}>
                        <FieldLabel htmlFor={field.name} className="text-foreground">Contraseña</FieldLabel>
                        <PasswordInput
                            id={field.name}
                            placeholder="••••••••"
                            autoComplete="current-password"
                            error={fieldState.error?.message}
                            aria-invalid={fieldState.invalid}
                            {...field}
                        />
                        {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                    </Field>
                )}
            />

            <Button type="submit" disabled={isSubmitting} className="w-full font-medium h-10">
                {isSubmitting ? 'Ingresando...' : 'Ingresar'}
            </Button>

            <div className="text-center pt-2">
                <button
                    type="button"
                    onClick={() => onRecuperarPassword(form.getValues('email'))}
                    className="text-sm text-muted-foreground hover:text-primary underline-offset-4 hover:underline transition-colors"
                >
                    ¿Olvidaste tu contraseña?
                </button>
            </div>
        </form>
    );
};
