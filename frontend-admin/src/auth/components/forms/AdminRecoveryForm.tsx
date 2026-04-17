import { useForm, Controller } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Field, FieldLabel, FieldError } from '@/components/ui/field';
import { emailOnlySchema as recoverySchema, type EmailOnlySchemaType as RecoverySchemaType } from '../../utils/authSchemas';

interface Props {
    email: string;
    isSubmitting: boolean;
    onSubmit: (data: RecoverySchemaType) => void;
    onVolverLogin: () => void;
}

export const AdminRecoveryForm = ({ email, isSubmitting, onSubmit, onVolverLogin }: Props) => {
    const form = useForm<RecoverySchemaType>({
        resolver: zodResolver(recoverySchema),
        defaultValues: {
            email
        }
    });

    return (
        <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-6">
            <Controller
                name="email"
                control={form.control}
                render={({ field, fieldState }) => (
                    <Field data-invalid={fieldState.invalid}>
                        <FieldLabel htmlFor={field.name}>Correo electrónico</FieldLabel>
                        <Input
                            id={field.name}
                            type="email"
                            placeholder="Correo electrónico"
                            readOnly={!!email}
                            autoComplete="email"
                            className={email ? "bg-muted/50" : ""}
                            aria-invalid={fieldState.invalid}
                            {...field}
                        />
                        {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                    </Field>
                )}
            />

            <div className="space-y-3">
                <Button type="submit" disabled={isSubmitting} loading={isSubmitting} className="w-full">
                    {isSubmitting ? "Enviando..." : "Enviar código"}
                </Button>

                <Button
                    type="button"
                    variant="link"
                    onClick={onVolverLogin}
                    disabled={isSubmitting}
                    className="w-full text-center"
                >
                    Volver a iniciar sesión
                </Button>
            </div>
        </form>
    );
};
