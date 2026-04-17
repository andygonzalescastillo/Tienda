import { Skeleton } from "@/components/ui/skeleton";

export const SessionSkeleton = () => {
    return (
        <div className="flex items-start justify-between border border-border p-4 rounded-xl bg-card">
            <div className="flex gap-4 flex-1">
                <Skeleton className="h-12 w-12 rounded-lg shrink-0" />
                <div className="flex-1 space-y-3">
                    <div className="flex gap-2">
                        <Skeleton className="h-5 w-32" />
                        <Skeleton className="h-5 w-16 rounded-full" />
                    </div>
                    <div className="space-y-2">
                        <Skeleton className="h-3 w-24" />
                        <Skeleton className="h-3 w-40" />
                        <Skeleton className="h-3 w-48" />
                    </div>
                </div>
            </div>
            <Skeleton className="h-8 w-8 rounded-md ml-2" />
        </div>
    );
};
