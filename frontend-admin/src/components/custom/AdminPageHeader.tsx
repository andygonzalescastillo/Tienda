import type { ReactNode } from 'react';
import { Link } from 'react-router-dom';
import { ROUTES } from '@/router/routes';
import { Breadcrumb, BreadcrumbItem, BreadcrumbLink, BreadcrumbList, BreadcrumbPage, BreadcrumbSeparator } from '@/components/ui/breadcrumb';
import { ThemeToggle } from '@/components/theme/ThemeToggle';
import { AdminMenu } from '@/admin/components/AdminMenu';

interface BreadcrumbEntry {
    label: string;
    to?: string;
}

interface Props {
    breadcrumbs: BreadcrumbEntry[];
    children?: ReactNode;
}

export const AdminPageHeader = ({ breadcrumbs, children }: Props) => (
    <header className="sticky top-0 z-30 flex h-14 items-center border-b bg-background/95 backdrop-blur supports-backdrop-filter:bg-background/60">
        <div className="flex items-center justify-between w-full max-w-4xl mx-auto px-4">
            <Breadcrumb className="flex-1">
                <BreadcrumbList>
                    <BreadcrumbItem>
                        <BreadcrumbLink asChild>
                            <Link to={ROUTES.ADMIN.DASHBOARD}>Panel</Link>
                        </BreadcrumbLink>
                    </BreadcrumbItem>
                    {breadcrumbs.map((crumb, i) => (
                        <span key={crumb.label} className="contents">
                            <BreadcrumbSeparator />
                            <BreadcrumbItem>
                                {i === breadcrumbs.length - 1 || !crumb.to ? (
                                    <BreadcrumbPage>{crumb.label}</BreadcrumbPage>
                                ) : (
                                    <BreadcrumbLink asChild>
                                        <Link to={crumb.to}>{crumb.label}</Link>
                                    </BreadcrumbLink>
                                )}
                            </BreadcrumbItem>
                        </span>
                    ))}
                </BreadcrumbList>
            </Breadcrumb>
            <div className="flex items-center gap-2">
                <ThemeToggle />
                {children}
                <AdminMenu />
            </div>
        </div>
    </header>
);
