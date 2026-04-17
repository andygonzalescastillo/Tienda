import { Separator } from '@/components/ui/separator';

export const OrSeparator = () => (
    <div className="flex items-center my-4">
        <Separator className="flex-1" />
        <span className="px-4 text-muted-foreground text-sm">o</span>
        <Separator className="flex-1" />
    </div>
);
