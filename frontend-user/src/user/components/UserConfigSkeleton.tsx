import { Skeleton } from "@/components/ui/skeleton";

export const UserConfigSkeleton = () => {
    return (
        <div className="min-h-screen bg-background">
            <header className="border-b bg-card px-4 py-8">
                <div className="max-w-4xl mx-auto space-y-4">
                    <Skeleton className="h-4 w-48" />
                    <Skeleton className="h-10 w-80" />
                </div>
            </header>
            <main className="max-w-4xl mx-auto px-4 py-10 space-y-8">
                <div className="border rounded-xl p-0 bg-card">
                    <div className="p-6 border-b">
                        <Skeleton className="h-6 w-48" />
                    </div>
                    <div className="p-6 grid sm:grid-cols-2 gap-8">
                        {[1, 2].map(i => (
                            <div key={i} className="space-y-2">
                                <Skeleton className="h-3 w-28 uppercase tracking-wider" />
                                <Skeleton className="h-12 w-full rounded-lg" />
                            </div>
                        ))}
                    </div>
                </div>

                <div className="border rounded-xl bg-card overflow-hidden">
                    <div className="p-4 border-b bg-muted/30">
                        <Skeleton className="h-5 w-32" />
                    </div>
                    <div className="p-6 flex items-center justify-between">
                        <div className="flex items-center gap-4">
                            <Skeleton className="w-12 h-12 rounded-xl" />
                            <div className="space-y-2">
                                <Skeleton className="h-5 w-32 font-bold" />
                                <Skeleton className="h-4 w-64" />
                            </div>
                        </div>
                        <Skeleton className="w-5 h-5 rounded-full" />
                    </div>
                </div>
            </main>
        </div>
    );
};
