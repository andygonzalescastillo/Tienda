import { useForm, Controller } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Button } from '@/components/ui/button';
import { Field, FieldLabel, FieldError, FieldGroup } from '@/components/ui/field';
import { newPasswordSchema, type NewPasswordSchemaType } from '../../utils/authSchemas';
import { PasswordInput } from '@/components/ui/password-input';
import { PasswordStrength } from '@/components/custom/PasswordStrength';

interface Props {
    email: string;
    isSubmitting: boolean;
    onSubmit: (data: NewPasswordSchemaType) => void;
}

export const NewPasswordForm = ({ email, isSubmitting, onSubmit }: Props) => {
    const form = useForm<NewPasswordSchemaType>({
        resolver: zodResolver(newPasswordSchema),
        mode: 'onBlur',
        defaultValues: {
            password: '',
            confirmPassword: ''
        }
    });

    return (
        <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-6">
            <div className="text-center space-y-1">
                <p className="text-muted-foreground font-medium bg-muted/50 py-1.5 px-3 rounded-full w-fit mx-auto">{email}</p>
                <p className="text-foreground font-medium pt-2">Crea una nueva contraseña fuerte y segura.</p>
            </div>

            <FieldGroup>
                <Controller
                    name="password"
                    control={form.control}
                    render={({ field, fieldState }) => (
                        <Field data-invalid={fieldState.invalid}>
                            <FieldLabel htmlFor={field.name}>Nueva contraseña</FieldLabel>
                            <PasswordInput
                                id={field.name}
                                placeholder="Nueva contraseña"
                                autoComplete="new-password"
                                aria-invalid={fieldState.invalid}
                                {...field}
                            />
                            <PasswordStrength password={field.value} />
                            {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                        </Field>
                    )}
                />

                <Controller
                    name="confirmPassword"
                    control={form.control}
                    render={({ field, fieldState }) => (
                        <Field data-invalid={fieldState.invalid}>
                            <FieldLabel htmlFor={field.name}>Confirmar contraseña</FieldLabel>
                            <PasswordInput
                                id={field.name}
                                placeholder="Confirmar contraseña"
                                autoComplete="new-password"
                                aria-invalid={fieldState.invalid}
                                {...field}
                            />
                            {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                        </Field>
                    )}
                />


            </FieldGroup>

            <div className="pt-2">
                <Button type="submit" disabled={isSubmitting} loading={isSubmitting} className="w-full font-medium h-11 text-md">
                    {isSubmitting ? "Restableciendo..." : "Restablecer contraseña"}
                </Button>
            </div>
        </form>
    );
};
