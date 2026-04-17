import type { ReactNode } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { ThemeToggle } from '@/components/theme/ThemeToggle';
import { Shield } from 'lucide-react';

interface Props {
    children: ReactNode;
    titulo: string;
    descripcion?: string;
}

export const LoginContainer = ({ children, titulo, descripcion }: Props) => (
    <main className="min-h-screen flex flex-col items-center justify-center bg-muted/20 p-4 relative">
        <div className="absolute top-4 right-4 z-50">
            <ThemeToggle />
        </div>

        <div className="mb-8 flex items-center justify-center gap-2 text-primary">
            <div className="bg-primary/10 p-2 rounded-xl">
                <Shield className="h-8 w-8" />
            </div>
            <span className="text-2xl font-bold tracking-tight text-foreground">Admin Panel</span>
        </div>

        <Card className="w-full max-w-md border-border/40 shadow-xl shadow-black/5 dark:shadow-none bg-card/95 backdrop-blur supports-backdrop-filter:bg-card/50">
            <CardHeader className="space-y-2 pb-6 text-center">
                <CardTitle className="text-2xl font-bold tracking-tight">{titulo}</CardTitle>
                {descripcion && (
                    <CardDescription className="text-base text-muted-foreground">
                        {descripcion}
                    </CardDescription>
                )}
            </CardHeader>
            <CardContent className="pb-8">
                {children}
            </CardContent>
        </Card>
    </main>
);
