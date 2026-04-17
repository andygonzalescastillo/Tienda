import { useMemo } from 'react';
import { Check, X } from 'lucide-react';
import { cn } from '@/lib/utils';

interface PasswordStrengthProps {
    password: string;
}

const RULES = [
    { label: 'Mínimo 8 caracteres', test: (p: string) => p.length >= 8 },
    { label: 'Contiene letras', test: (p: string) => /[a-zA-Z]/.test(p) },
    { label: 'Contiene números', test: (p: string) => /\d/.test(p) },
];

const STRENGTH: Record<number, { label: string; color: string; textColor: string }> = {
    0: { label: 'Muy débil', color: 'bg-destructive', textColor: 'text-destructive' },
    1: { label: 'Débil', color: 'bg-orange-500', textColor: 'text-orange-500' },
    2: { label: 'Aceptable', color: 'bg-yellow-500', textColor: 'text-yellow-600 dark:text-yellow-500' },
    3: { label: 'Fuerte', color: 'bg-emerald-500', textColor: 'text-emerald-500' },
};

export const PasswordStrength = ({ password }: PasswordStrengthProps) => {
    const results = useMemo(() => RULES.map(r => r.test(password)), [password]);
    const score = results.filter(Boolean).length;
    const { label, color, textColor } = STRENGTH[score];

    if (!password) return null;

    return (
        <div className="space-y-2.5 pt-1 animate-in fade-in slide-in-from-top-1 duration-200">
            <div className="space-y-1">
                <div className="flex gap-1">
                    {RULES.map((_, i) => (
                        <div
                            key={i}
                            className={cn(
                                "h-1 flex-1 rounded-full transition-all duration-300",
                                i < score ? color : "bg-muted"
                            )}
                        />
                    ))}
                </div>
                <p className={cn("text-xs font-medium transition-colors", textColor)}>
                    {label}
                </p>
            </div>

            <ul className="grid grid-cols-2 gap-x-2 gap-y-1">
                {RULES.map((rule, i) => (
                    <li key={rule.label} className="flex items-center gap-1.5 text-xs">
                        {results[i] ? (
                            <Check className="w-3.5 h-3.5 text-emerald-500 shrink-0" />
                        ) : (
                            <X className="w-3.5 h-3.5 text-muted-foreground/50 shrink-0" />
                        )}
                        <span className={cn(
                            "transition-colors",
                            results[i] ? "text-foreground" : "text-muted-foreground"
                        )}>
                            {rule.label}
                        </span>
                    </li>
                ))}
            </ul>
        </div>
    );
};
