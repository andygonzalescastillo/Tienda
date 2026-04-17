import { Skeleton } from "@/components/ui/skeleton";
import { ThemeToggle } from '@/components/theme/ThemeToggle';
import { ShoppingBag } from 'lucide-react';

interface AuthSkeletonProps {
    variant?: 'login' | 'register' | 'password' | 'verification' | 'choose-method' | 'recovery' | 'reset-password';
}

export const AuthSkeleton = ({ variant = 'login' }: AuthSkeletonProps) => {
    return (
        <div className="min-h-screen flex flex-col items-center justify-center bg-muted/20 p-4 relative">
            <div className="absolute top-4 right-4 z-50">
                <ThemeToggle />
            </div>

            <div className="mb-8 flex items-center justify-center gap-2 text-primary opacity-50">
                <div className="bg-primary/10 p-2 rounded-xl">
                    <ShoppingBag className="h-8 w-8" />
                </div>
                <span className="text-2xl font-bold tracking-tight text-foreground">Mi Tienda</span>
            </div>

            <div className="w-full max-w-md bg-card/95 backdrop-blur supports-backdrop-filter:bg-card/50 p-6 rounded-xl border border-border/40 shadow-xl shadow-black/5 dark:shadow-none space-y-6">
                <div className="space-y-4 text-center">
                    <Skeleton className="h-8 w-64 mx-auto" />
                </div>

                {variant === 'login' && (
                    <div className="space-y-4">
                        <div className="space-y-3">
                            {[1, 2, 3, 4].map((i) => (
                                <Skeleton key={i} className="h-10 w-full rounded-md" />
                            ))}
                        </div>

                        <div className="flex items-center gap-4 py-2">
                            <Skeleton className="h-px flex-1" />
                            <Skeleton className="h-4 w-4" />
                            <Skeleton className="h-px flex-1" />
                        </div>

                        <div className="space-y-4">
                            <Skeleton className="h-10 w-full rounded-md" />
                            <Skeleton className="h-10 w-full rounded-md font-medium" />
                        </div>
                    </div>
                )}

                {variant === 'register' && (
                    <div className="space-y-4">
                        <div className="space-y-3">
                            {[1, 2, 3, 4].map((i) => (
                                <div key={i} className="space-y-2">
                                    <Skeleton className="h-4 w-24" />
                                    <Skeleton className="h-10 w-full rounded-md" />
                                </div>
                            ))}
                        </div>
                        <div className="pt-2">
                            <Skeleton className="h-10 w-full rounded-md" />
                        </div>
                    </div>
                )}

                {variant === 'password' && (
                    <div className="space-y-6">
                        <div className="space-y-2">
                            <Skeleton className="h-4 w-32" />
                            <div className="relative">
                                <Skeleton className="h-10 w-full rounded-md" />
                                <Skeleton className="absolute right-1 top-1 h-8 w-16 rounded shadow-none" />
                            </div>
                        </div>
                        <div className="space-y-2">
                            <Skeleton className="h-4 w-24" />
                            <Skeleton className="h-10 w-full rounded-md" />
                        </div>
                        <Skeleton className="h-4 w-40" />
                        <Skeleton className="h-10 w-full rounded-md" />
                    </div>
                )}

                {variant === 'verification' && (
                    <div className="space-y-8">
                        <Skeleton className="h-6 w-56 mx-auto" />

                        <div className="p-4 rounded-lg border border-primary/20 bg-primary/5 flex items-start gap-3">
                            <Skeleton className="h-5 w-5 rounded-full mt-0.5" />
                            <div className="space-y-2 flex-1">
                                <Skeleton className="h-4 w-24" />
                                <Skeleton className="h-3 w-48" />
                            </div>
                        </div>

                        <div className="flex justify-center gap-2 py-2">
                            {[1, 2, 3, 4, 5, 6].map((i) => (
                                <Skeleton key={i} className="h-12 w-10 sm:w-12 rounded-md border" />
                            ))}
                        </div>

                        <div className="space-y-3 pt-2">
                            <Skeleton className="h-10 w-full rounded-md" />
                            <Skeleton className="h-10 w-full rounded-md" />
                        </div>
                    </div>
                )}

                {variant === 'choose-method' && (
                    <div className="space-y-6">
                        <div className="space-y-2 text-center pb-2">
                            <Skeleton className="h-4 w-48 mx-auto" />
                            <Skeleton className="h-5 w-64 mx-auto font-medium" />
                        </div>

                        <div className="space-y-3">
                            {[1, 2, 3, 4].map((i) => (
                                <Skeleton key={i} className="h-10 w-full rounded-md" />
                            ))}
                        </div>

                        <div className="space-y-6 pt-4">
                            <div className="flex items-center gap-4">
                                <Skeleton className="h-px flex-1" />
                                <Skeleton className="h-4 w-4" />
                                <Skeleton className="h-px flex-1" />
                            </div>
                            <div className="space-y-4">
                                <div className="space-y-2">
                                    <Skeleton className="h-4 w-24" />
                                    <Skeleton className="h-10 w-full rounded-md" />
                                </div>
                                <Skeleton className="h-10 w-full rounded-md" />
                                <Skeleton className="h-4 w-32 mx-auto" />
                            </div>
                        </div>
                    </div>
                )}

                {variant === 'recovery' && (
                    <div className="space-y-8">
                        <Skeleton className="h-4 w-3/4 mx-auto" />
                        <div className="space-y-2">
                            <Skeleton className="h-4 w-32" />
                            <Skeleton className="h-10 w-full rounded-md" />
                        </div>
                        <div className="space-y-3">
                            <Skeleton className="h-10 w-full rounded-md" />
                            <Skeleton className="h-10 w-full rounded-md" />
                        </div>
                    </div>
                )}

                {variant === 'reset-password' && (
                    <div className="space-y-8">
                        <div className="space-y-2 text-center">
                            <Skeleton className="h-5 w-48 mx-auto" />
                            <Skeleton className="h-4 w-64 mx-auto" />
                        </div>
                        <div className="space-y-5">
                            {[1, 2].map(i => (
                                <div key={i} className="space-y-2">
                                    <Skeleton className="h-4 w-32" />
                                    <Skeleton className="h-10 w-full rounded-md" />
                                </div>
                            ))}
                            <Skeleton className="h-20 w-full rounded-md border p-3 flex flex-col gap-2">
                                <Skeleton className="h-3 w-full" />
                                <Skeleton className="h-3 w-5/6" />
                            </Skeleton>
                        </div>
                        <Skeleton className="h-10 w-full rounded-md pt-2" />
                    </div>
                )}
            </div>
        </div>
    );
};
