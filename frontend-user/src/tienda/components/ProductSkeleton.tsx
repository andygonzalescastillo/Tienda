import { Skeleton } from "@/components/ui/skeleton";
import { Card, CardContent, CardFooter } from "@/components/ui/card";

export const ProductSkeleton = () => {
    return (
        <Card className="overflow-hidden border-border bg-card shadow-none">
            <CardContent className="p-0">
                <div className="bg-muted aspect-video flex items-center justify-center">
                    <Skeleton className="h-12 w-12 rounded-full opacity-50" />
                </div>
                <div className="p-6 space-y-3">
                    <Skeleton className="h-6 w-3/4 font-bold" />
                    <div className="space-y-2">
                        <Skeleton className="h-4 w-full" />
                        <Skeleton className="h-4 w-5/6" />
                    </div>
                </div>
            </CardContent>
            <CardFooter className="flex justify-between items-center p-6 pt-0">
                <Skeleton className="h-8 w-24" />
                <Skeleton className="h-9 w-24 rounded-md" />
            </CardFooter>
        </Card>
    );
};
