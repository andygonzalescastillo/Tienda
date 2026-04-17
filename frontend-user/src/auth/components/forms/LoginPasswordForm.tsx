import { useForm, Controller } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Field, FieldLabel, FieldError } from '@/components/ui/field';
import { loginPasswordSchema, type LoginPasswordSchemaType } from '../../utils/authSchemas';
import { PasswordInput } from '@/components/ui/password-input';

interface Props {
    email: string;
    isSubmitting: boolean;
    onSubmit: (data: LoginPasswordSchemaType) => void;
    onRecuperarPassword?: () => void;
    onEditarEmail: () => void;
}

export const LoginPasswordForm = ({ email, isSubmitting, onSubmit, onRecuperarPassword, onEditarEmail }: Props) => {
    const form = useForm<LoginPasswordSchemaType>({
        resolver: zodResolver(loginPasswordSchema),
        defaultValues: {
            password: ''
        }
    });

    return (
        <section aria-labelledby="login-password-title">
            <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-6">
                <div className="space-y-2">
                    <div className="flex items-center justify-between">
                        <FieldLabel className="text-foreground font-medium">Correo electrónico</FieldLabel>
                        <Button
                            type="button"
                            variant="ghost"
                            size="sm"
                            onClick={onEditarEmail}
                            disabled={isSubmitting}
                            className="h-6 px-2 text-xs text-primary hover:text-primary hover:bg-primary/10 transition-colors"
                        >
                            Cambiar correo
                        </Button>
                    </div>
                    <Input
                        type="email"
                        value={email}
                        readOnly
                        autoComplete="email"
                        className="bg-muted/50 h-10 text-muted-foreground"
                    />
                </div>

                <Controller
                    name="password"
                    control={form.control}
                    render={({ field, fieldState }) => (
                        <Field data-invalid={fieldState.invalid}>
                            <FieldLabel htmlFor={field.name} className="text-foreground">Contraseña</FieldLabel>
                            <PasswordInput
                                id={field.name}
                                placeholder="Contraseña"
                                autoComplete="current-password"
                                autoFocus
                                aria-invalid={fieldState.invalid}
                                {...field}
                            />
                            {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                        </Field>
                    )}
                />

                {onRecuperarPassword && (
                    <Button
                        type="button"
                        variant="link"
                        onClick={onRecuperarPassword}
                        className="px-0 text-primary hover:underline h-auto font-medium"
                    >
                        ¿Olvidaste tu contraseña?
                    </Button>
                )}

                <div className="pt-2">
                    <Button type="submit" disabled={isSubmitting} loading={isSubmitting} className="w-full font-medium h-11 text-md">
                        {isSubmitting ? "Entrando..." : "Iniciar Sesión"}
                    </Button>
                </div>
            </form>
        </section>
    );
};