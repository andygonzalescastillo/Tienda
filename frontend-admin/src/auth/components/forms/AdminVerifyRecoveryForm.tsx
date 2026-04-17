import { Button } from '@/components/ui/button';
import { VerificationCode } from './VerificationCode';
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert';
import { Mail } from 'lucide-react';

interface Props {
    email: string;
    codigo: string;
    onCodigoChange: (value: string) => void;
    onVerificar: () => void;
    onReenviar: () => void;
    cargando: boolean;
    cooldown?: number;
}

export const AdminVerifyRecoveryForm = ({
    email,
    codigo,
    onCodigoChange,
    onVerificar,
    onReenviar,
    cargando,
    cooldown = 0
}: Props) => {
    return (
        <section className="space-y-6" aria-labelledby="verification-title">
            <Alert className="bg-primary/10 border-primary/20 text-primary">
                <Mail className="h-4 w-4" />
                <AlertTitle>Código enviado</AlertTitle>
                <AlertDescription>
                    Enviamos un código de 6 dígitos a: <span className="font-semibold block mt-1">{email}</span>
                </AlertDescription>
            </Alert>

            <div className="flex justify-center py-2">
                <VerificationCode
                    codigo={codigo}
                    onChange={onCodigoChange}
                    onComplete={onVerificar}
                    cargando={cargando}
                />
            </div>

            <div className="flex flex-col gap-3 pt-4">
                <Button
                    onClick={onVerificar}
                    disabled={cargando || codigo.length !== 6}
                    loading={cargando}
                    className="w-full font-medium h-11 text-md"
                >
                    {cargando ? "Verificando..." : "Verificar código"}
                </Button>

                <Button
                    variant="link"
                    onClick={onReenviar}
                    disabled={cargando || cooldown > 0}
                    className="w-full"
                >
                    {cooldown > 0
                        ? `Reenviar en (${cooldown}s)`
                        : "¿No recibiste el código? Reenviar"
                    }
                </Button>
            </div>
        </section>
    );
};
