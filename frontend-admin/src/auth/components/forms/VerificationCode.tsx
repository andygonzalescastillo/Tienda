import { REGEXP_ONLY_DIGITS } from "input-otp";
import { InputOTP, InputOTPGroup, InputOTPSlot, InputOTPSeparator } from "@/components/ui/input-otp";

interface Props {
    codigo: string;
    onChange: (value: string) => void;
    onComplete?: () => void;
    cargando?: boolean;
}

export const VerificationCode = ({ codigo, onChange, onComplete, cargando }: Props) => {
    return (
        <div className="mb-6 flex justify-center">
            <InputOTP
                maxLength={6}
                value={codigo}
                onChange={onChange}
                onComplete={onComplete}
                disabled={cargando}
                pattern={REGEXP_ONLY_DIGITS}
            >
                <InputOTPGroup>
                    {[0, 1, 2].map(i => (
                        <InputOTPSlot key={i} index={i} className="w-12 h-12 text-xl" />
                    ))}
                </InputOTPGroup>
                <InputOTPSeparator />
                <InputOTPGroup>
                    {[3, 4, 5].map(i => (
                        <InputOTPSlot key={i} index={i} className="w-12 h-12 text-xl" />
                    ))}
                </InputOTPGroup>
            </InputOTP>
        </div>
    );
};
