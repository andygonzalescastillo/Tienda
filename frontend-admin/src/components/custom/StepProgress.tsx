import { Check } from 'lucide-react';
import { cn } from '@/lib/utils';

interface StepProgressProps {
    steps: readonly { label: string }[];
    currentStep: number;
}

export const StepProgress = ({ steps, currentStep }: StepProgressProps) => (
    <div className="flex items-center justify-between w-full mb-6" role="navigation" aria-label="Progreso">
        {steps.map((step, index) => {
            const isCompleted = index < currentStep;
            const isCurrent = index === currentStep;

            return (
                <div key={step.label} className="flex items-center flex-1 last:flex-none">
                    <div className="flex flex-col items-center gap-1.5">
                        <div
                            className={cn(
                                "w-8 h-8 rounded-full flex items-center justify-center text-xs font-bold transition-all duration-300 shrink-0",
                                isCompleted && "bg-primary text-primary-foreground shadow-md shadow-primary/25",
                                isCurrent && "bg-primary/15 text-primary ring-2 ring-primary",
                                !isCompleted && !isCurrent && "bg-muted text-muted-foreground"
                            )}
                            aria-current={isCurrent ? 'step' : undefined}
                        >
                            {isCompleted ? <Check className="w-4 h-4" /> : index + 1}
                        </div>
                        <span
                            className={cn(
                                "text-[11px] font-medium text-center whitespace-nowrap transition-colors",
                                isCurrent && "text-primary",
                                isCompleted && "text-foreground",
                                !isCompleted && !isCurrent && "text-muted-foreground"
                            )}
                        >
                            {step.label}
                        </span>
                    </div>

                    {index < steps.length - 1 && (
                        <div className="flex-1 mx-2 -mt-4.5">
                            <div className="h-0.5 w-full bg-muted rounded-full overflow-hidden">
                                <div
                                    className={cn(
                                        "h-full bg-primary rounded-full transition-all duration-500",
                                        isCompleted ? "w-full" : "w-0"
                                    )}
                                />
                            </div>
                        </div>
                    )}
                </div>
            );
        })}
    </div>
);
