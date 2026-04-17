import { Skeleton } from '@/components/ui/skeleton';

export const PageSkeleton = () => (
    <div className="min-h-screen flex items-center justify-center bg-background">
        <Skeleton className="w-full max-w-md h-96 rounded-lg" />
    </div>
);
