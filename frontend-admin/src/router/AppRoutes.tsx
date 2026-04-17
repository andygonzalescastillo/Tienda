import { createBrowserRouter } from 'react-router-dom';
import { lazy, Suspense } from 'react';
import { ROUTES } from './routes';
import { PublicRoute } from '@/router/guards/PublicRoute';
import { AdminProtectedRoute } from '@/router/guards/AdminProtectedRoute';
import { GlobalError } from '@/components/custom/GlobalError';
import { PageSkeleton } from '@/components/custom/PageSkeleton';

const AdminLogin = lazy(() => import('@/auth/pages/AdminLogin').then(m => ({ default: m.AdminLogin })));
const AdminRecovery = lazy(() => import('@/auth/pages/AdminRecovery').then(m => ({ default: m.AdminRecovery })));
const AdminVerifyRecovery = lazy(() => import('@/auth/pages/AdminVerifyRecovery').then(m => ({ default: m.AdminVerifyRecovery })));
const AdminNewPassword = lazy(() => import('@/auth/pages/AdminNewPassword').then(m => ({ default: m.AdminNewPassword })));

const Dashboard = lazy(() => import('@/admin/pages/Dashboard').then(m => ({ default: m.Dashboard })));
const AdminConfig = lazy(() => import('@/admin/pages/AdminConfig').then(m => ({ default: m.AdminConfig })));
const AdminSessions = lazy(() => import('@/admin/pages/AdminSessions').then(m => ({ default: m.AdminSessions })));
const AdminUsers = lazy(() => import('@/admin/pages/AdminUsers').then(m => ({ default: m.AdminUsers })));
const NotFound = lazy(() => import('@/components/custom/NotFound').then(m => ({ default: m.NotFound })));

export const router = createBrowserRouter([
    {
        element: <PublicRoute />,
        errorElement: <GlobalError />,
        children: [
            { path: ROUTES.AUTH.LOGIN, element: <Suspense fallback={<PageSkeleton />}><AdminLogin /></Suspense> },
            { path: ROUTES.AUTH.RECOVERY, element: <Suspense fallback={<PageSkeleton />}><AdminRecovery /></Suspense> },
            { path: ROUTES.AUTH.VERIFY_RECOVERY, element: <Suspense fallback={<PageSkeleton />}><AdminVerifyRecovery /></Suspense> },
            { path: ROUTES.AUTH.NEW_PASSWORD, element: <Suspense fallback={<PageSkeleton />}><AdminNewPassword /></Suspense> },
        ]
    },

    {
        element: <AdminProtectedRoute />,
        errorElement: <GlobalError />,
        children: [
            { path: ROUTES.ADMIN.DASHBOARD, element: <Suspense fallback={<PageSkeleton />}><Dashboard /></Suspense> },
            { path: ROUTES.ADMIN.CONFIGURATION, element: <Suspense fallback={<PageSkeleton />}><AdminConfig /></Suspense> },
            { path: ROUTES.ADMIN.SESSIONS, element: <Suspense fallback={<PageSkeleton />}><AdminSessions /></Suspense> },
            { path: ROUTES.ADMIN.USERS, element: <Suspense fallback={<PageSkeleton />}><AdminUsers /></Suspense> },
        ]
    },
    { path: '*', element: <Suspense fallback={<PageSkeleton />}><NotFound /></Suspense>, errorElement: <GlobalError /> }
]);
