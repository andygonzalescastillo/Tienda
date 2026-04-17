import type { ComponentProps } from 'react';
import { Google } from '@/components/icons/Google';
import { Microsoft } from '@/components/icons/Microsoft';
import { Facebook } from '@/components/icons/Facebook';
import { Github } from '@/components/icons/Github';
import { Button } from '@/components/ui/button';
import { AUTH_PROVIDERS, SOCIAL_PROVIDERS_LIST } from '@/core/constants/authConstants';

export interface Handlers {
    onGoogleClick: () => void;
    onMicrosoftClick: () => void;
    onFacebookClick: () => void;
    onGithubClick: () => void;
}

interface Props {
    proveedoresFiltrados?: string[] | null;
    handlers: Handlers;
    cargando: boolean;
}

type ButtonVariant = ComponentProps<typeof Button>['variant'];

const PROVIDER_CONFIG: Record<string, {
    label: string;
    variant: ButtonVariant;
    Icon: React.ComponentType<React.SVGProps<SVGSVGElement>>;
    handlerKey: keyof Handlers;
}> = {
    [AUTH_PROVIDERS.GOOGLE]: {
        label: 'Continuar con Google',
        variant: 'outline',
        Icon: Google,
        handlerKey: 'onGoogleClick'
    },
    [AUTH_PROVIDERS.MICROSOFT]: {
        label: 'Continuar con Microsoft',
        variant: 'outline',
        Icon: Microsoft,
        handlerKey: 'onMicrosoftClick'
    },
    [AUTH_PROVIDERS.FACEBOOK]: {
        label: 'Continuar con Facebook',
        variant: 'facebook',
        Icon: Facebook,
        handlerKey: 'onFacebookClick'
    },
    [AUTH_PROVIDERS.GITHUB]: {
        label: 'Continuar con GitHub',
        variant: 'github',
        Icon: Github,
        handlerKey: 'onGithubClick'
    }
};

export const ListaProveedoresOAuth = ({ proveedoresFiltrados, handlers, cargando }: Props) => {
    const activeProviders = proveedoresFiltrados ? SOCIAL_PROVIDERS_LIST.filter(p => proveedoresFiltrados.includes(p)) : SOCIAL_PROVIDERS_LIST;

    return (
        <section aria-label="Proveedores de autenticación externa">
            {activeProviders.map(key => {
                const config = PROVIDER_CONFIG[key];
                if (!config) return null;

                const { label, variant, Icon, handlerKey } = config;

                return (
                    <Button
                        key={key}
                        type="button"
                        variant={variant}
                        onClick={handlers[handlerKey]}
                        disabled={cargando}
                        className="w-full mb-3"
                    >
                        <Icon className="w-5 h-5 mr-2 shrink-0" />
                        <span>{label}</span>
                    </Button>
                );
            })}
        </section>
    );
};