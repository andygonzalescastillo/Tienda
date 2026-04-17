import { Skeleton } from "@/components/ui/skeleton";
import { ProductSkeleton } from "./ProductSkeleton";

export const LandingSkeleton = () => {
    return (
        <div className="min-h-screen bg-background flex flex-col">
            <header className="h-16 border-b bg-background/95 sticky top-0 z-50">
                <div className="max-w-7xl mx-auto px-4 h-full flex items-center justify-between">
                    <div className="flex items-center gap-2">
                        <Skeleton className="h-6 w-6 rounded-md" />
                        <Skeleton className="h-6 w-24" />
                    </div>
                    <Skeleton className="h-9 w-28 rounded-md" />
                </div>
            </header>

            <main className="flex-1 max-w-7xl mx-auto px-4 py-12 md:py-20 w-full space-y-12">
                <div className="text-center space-y-6 mb-16">
                    <Skeleton className="h-12 w-3/4 max-w-125 mx-auto" />
                    <Skeleton className="h-5 w-full max-w-150 mx-auto" />
                </div>

                <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-8">
                    {[1, 2, 3, 4, 5, 6].map((i) => (
                        <ProductSkeleton key={i} />
                    ))}
                </div>
            </main>

            <footer className="border-t bg-card py-12">
                <div className="max-w-7xl mx-auto px-4 flex flex-col items-center gap-6">
                    <div className="flex items-center gap-2 opacity-50">
                        <Skeleton className="h-5 w-5 rounded-md" />
                        <Skeleton className="h-5 w-20" />
                    </div>
                    <div className="space-y-2">
                        <Skeleton className="h-4 w-64 mx-auto" />
                        <Skeleton className="h-4 w-48 mx-auto" />
                    </div>
                </div>
            </footer>
        </div>
    );
};
