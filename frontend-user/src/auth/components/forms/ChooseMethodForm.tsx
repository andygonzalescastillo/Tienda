import { useForm, Controller } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Button } from '@/components/ui/button';
import { PasswordInput } from '@/components/ui/password-input';
import { OrSeparator } from '@/components/custom/OrSeparator';
import { Field, FieldLabel, FieldError } from '@/components/ui/field';
import { AUTH_PROVIDERS } from '@/core/constants/authConstants';
import { ListaProveedoresOAuth, type Handlers } from '../oauth/OAuthProviders';
import { loginPasswordSchema, type LoginPasswordSchemaType } from '../../utils/authSchemas';

interface Props {
    email: string;
    proveedoresVinculados: string[] | null;
    isSubmitting: boolean;
    onSubmit: (data: LoginPasswordSchemaType) => void;
    onRecuperarPassword?: () => void;
    oauthHandlers: Handlers;
    cargandoOAuth: boolean;
}

export const ChooseMethodForm = ({ email, proveedoresVinculados, isSubmitting, onSubmit, onRecuperarPassword, oauthHandlers, cargandoOAuth }: Props) => {
    const cargando = isSubmitting || cargandoOAuth;
    const showPassword = proveedoresVinculados?.includes(AUTH_PROVIDERS.LOCAL);

    const form = useForm<LoginPasswordSchemaType>({
        resolver: zodResolver(loginPasswordSchema),
        defaultValues: {
            password: ''
        }
    });

    return (
        <section aria-labelledby="choose-method-title">
            <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-6">
                <div className="text-center space-y-1 mb-6">
                    <p className="text-muted-foreground font-medium bg-muted/50 py-1.5 px-3 rounded-full w-fit mx-auto">{email}</p>
                    <h2 id="choose-method-title" className="text-foreground font-semibold pt-4">Elige cómo quieres entrar:</h2>
                </div>

                <ListaProveedoresOAuth
                    proveedoresFiltrados={proveedoresVinculados}
                    handlers={oauthHandlers}
                    cargando={cargando}
                />

                {showPassword && (
                    <>
                        <OrSeparator />
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
                                        aria-invalid={fieldState.invalid}
                                        {...field}
                                    />
                                    {fieldState.invalid && <FieldError errors={[fieldState.error]} />}
                                </Field>
                            )}
                        />

                        <div className="pt-2">
                            <Button type="submit" disabled={cargando} loading={isSubmitting} className="w-full font-medium h-11 text-md">
                                {isSubmitting ? "Entrando..." : "Entrar con contraseña"}
                            </Button>
                        </div>

                        {onRecuperarPassword && (
                            <Button
                                type="button"
                                variant="link"
                                onClick={onRecuperarPassword}
                                className="w-full text-center text-primary h-auto font-normal hover:underline"
                            >
                                ¿Olvidaste tu contraseña?
                            </Button>
                        )}
                    </>
                )}
            </form>
        </section>
    );
};