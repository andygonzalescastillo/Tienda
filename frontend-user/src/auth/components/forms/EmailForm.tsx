import { useForm, Controller } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { OrSeparator } from '@/components/custom/OrSeparator';
import { Field, FieldError } from '@/components/ui/field';
import { ListaProveedoresOAuth } from '../oauth/OAuthProviders';
import { emailOnlySchema, type EmailOnlySchemaType } from '../../utils/authSchemas';

interface Props {
    isSubmitting: boolean;
    onSubmit: (data: EmailOnlySchemaType) => void;
    onGoogleClick: () => void;
    onMicrosoftClick: () => void;
    onFacebookClick: () => void;
    onGithubClick: () => void;
    cargandoOAuth: boolean;
    defaultEmail?: string;
}

export const EmailForm = ({ isSubmitting, onSubmit, cargandoOAuth, defaultEmail = '', ...handlers }: Props) => {
    const cargando = isSubmitting || cargandoOAuth;

    const form = useForm<EmailOnlySchemaType>({
        resolver: zodResolver(emailOnlySchema),
        defaultValues: {
            email: defaultEmail
        }
    });

    return (
        <section aria-labelledby="email-form-title">
            <form onSubmit={form.handleSubmit(onSubmit)}>
                <ListaProveedoresOAuth handlers={handlers} cargando={cargando} />

                <OrSeparator />

                <Controller
                    name="email"
                    control={form.control}
                    render={({ field, fieldState }) => (
                        <Field className="mb-4" data-invalid={fieldState.invalid}>
                            <Input
                                id={field.name}
                                type="email"
                                placeholder="Correo electrónico"
                                autoComplete="email"
                                aria-invalid={fieldState.invalid}
                                {...field}
                            />
                            {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                        </Field>
                    )}
                />
                <div className="pt-2">
                    <Button type="submit" disabled={cargando} className="w-full font-medium h-11 text-md">
                        {isSubmitting ? "Verificando..." : "Continuar con Email"}
                    </Button>
                </div>
            </form>
        </section>
    );
};
