import { useForm, Controller } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Field, FieldLabel, FieldError, FieldGroup } from '@/components/ui/field';
import { registerSchema, type RegisterSchemaType } from '../../utils/authSchemas';
import { PasswordInput } from '@/components/ui/password-input';
import { PasswordStrength } from '@/components/custom/PasswordStrength';

interface Props {
    isSubmitting: boolean;
    onSubmit: (data: RegisterSchemaType) => void;
    defaultEmail?: string;
}

export const RegisterForm = ({ isSubmitting, onSubmit, defaultEmail = '' }: Props) => {
    const form = useForm<RegisterSchemaType>({
        resolver: zodResolver(registerSchema),
        mode: 'onBlur',
        defaultValues: {
            email: defaultEmail,
            nombre: '',
            apellido: '',
            password: ''
        }
    });

    return (
        <section aria-labelledby="register-form-title">
            <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
                <FieldGroup>
                    <Controller
                        name="email"
                        control={form.control}
                        render={({ field, fieldState }) => (
                            <Field data-invalid={fieldState.invalid}>
                                <FieldLabel htmlFor={field.name} className="text-foreground">Correo electrónico</FieldLabel>
                                <Input
                                    id={field.name}
                                    type="email"
                                    placeholder="correo@ejemplo.com"
                                    readOnly
                                    autoComplete="email"
                                    className="bg-muted/50"
                                    aria-invalid={fieldState.invalid}
                                    {...field}
                                />
                                {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                            </Field>
                        )}
                    />

                    <Controller
                        name="nombre"
                        control={form.control}
                        render={({ field, fieldState }) => (
                            <Field data-invalid={fieldState.invalid}>
                                <FieldLabel htmlFor={field.name} className="text-foreground">Nombre</FieldLabel>
                                <Input
                                    id={field.name}
                                    placeholder="Ej. Juan"
                                    autoComplete="given-name"
                                    aria-invalid={fieldState.invalid}
                                    {...field}
                                />
                                {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                            </Field>
                        )}
                    />

                    <Controller
                        name="apellido"
                        control={form.control}
                        render={({ field, fieldState }) => (
                            <Field data-invalid={fieldState.invalid}>
                                <FieldLabel htmlFor={field.name} className="text-foreground">Apellido</FieldLabel>
                                <Input
                                    id={field.name}
                                    placeholder="Ej. Pérez"
                                    autoComplete="family-name"
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
                                    placeholder="Mínimo 8 caracteres"
                                    autoComplete="new-password"
                                    aria-invalid={fieldState.invalid}
                                    {...field}
                                />
                                <PasswordStrength password={field.value} />
                                {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                            </Field>
                        )}
                    />
                </FieldGroup>

                <div className="pt-4">
                    <Button type="submit" className="w-full font-medium h-11 text-md" disabled={isSubmitting} loading={isSubmitting}>
                        {isSubmitting ? "Registrando..." : " Unirme ahora"}
                    </Button>
                </div>
            </form>
        </section>
    );
};