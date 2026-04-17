import { createBrowserRouter } from 'react-router-dom';
import { lazy, Suspense } from 'react';
import { ROUTES } from './routes';
import { PublicRoute } from '@/router/guards/PublicRoute';
import { UserProtectedRoute } from '@/router/guards/UserProtectedRoute';
import { GlobalError } from '@/components/custom/GlobalError';
import { AuthSkeleton } from '@/auth/components/layout/AuthSkeleton';
import { LandingSkeleton } from '@/tienda/components/LandingSkeleton';
import { UserConfigSkeleton } from '@/user/components/UserConfigSkeleton';

const LandingPage = lazy(() => import('@/tienda/pages/LandingPage').then(m => ({ default: m.LandingPage })));
const OAuthCallback = lazy(() => import('@/auth/pages/OAuthCallback').then(m => ({ default: m.OAuthCallback })));

const UserLogin = lazy(() => import('@/auth/pages/UserLogin').then(m => ({ default: m.UserLogin })));
const UserRegister = lazy(() => import('@/auth/pages/UserRegister').then(m => ({ default: m.UserRegister })));
const UserVerification = lazy(() => import('@/auth/pages/UserVerification').then(m => ({ default: m.UserVerification })));
const UserLoginPassword = lazy(() => import('@/auth/pages/UserLoginPassword').then(m => ({ default: m.UserLoginPassword })));
const UserChooseMethod = lazy(() => import('@/auth/pages/UserChooseMethod').then(m => ({ default: m.UserChooseMethod })));
const UserRecovery = lazy(() => import('@/auth/pages/UserRecovery').then(m => ({ default: m.UserRecovery })));
const UserVerifyRecovery = lazy(() => import('@/auth/pages/UserVerifyRecovery').then(m => ({ default: m.UserVerifyRecovery })));
const UserNewPassword = lazy(() => import('@/auth/pages/UserNewPassword').then(m => ({ default: m.UserNewPassword })));

const UserConfiguration = lazy(() => import('@/user/pages/UserConfiguration').then(m => ({ default: m.UserConfiguration })));
const UserSessions = lazy(() => import('@/user/pages/UserSessions').then(m => ({ default: m.UserSessions })));
const NotFound = lazy(() => import('@/components/custom/NotFound').then(m => ({ default: m.NotFound })));

export const router = createBrowserRouter([
    {
        path: ROUTES.HOME,
        element: (
            <Suspense fallback={<LandingSkeleton />}>
                <LandingPage />
            </Suspense>
        ),
        errorElement: <GlobalError />
    },
    {
        path: ROUTES.OAUTH_CALLBACK,
        element: (
            <Suspense fallback={<LandingSkeleton />}>
                <OAuthCallback />
            </Suspense>
        ),
        errorElement: <GlobalError />
    },

    {
        element: <PublicRoute />,
        errorElement: <GlobalError />,
        children: [
            { path: ROUTES.AUTH.LOGIN, element: <Suspense fallback={<AuthSkeleton variant="login" />}><UserLogin /></Suspense> },
            { path: ROUTES.AUTH.REGISTER, element: <Suspense fallback={<AuthSkeleton variant="register" />}><UserRegister /></Suspense> },
            { path: ROUTES.AUTH.VERIFY_EMAIL, element: <Suspense fallback={<AuthSkeleton variant="verification" />}><UserVerification /></Suspense> },
            { path: ROUTES.AUTH.LOGIN_PASSWORD, element: <Suspense fallback={<AuthSkeleton variant="password" />}><UserLoginPassword /></Suspense> },
            { path: ROUTES.AUTH.CHOOSE_METHOD, element: <Suspense fallback={<AuthSkeleton variant="choose-method" />}><UserChooseMethod /></Suspense> },
            { path: ROUTES.AUTH.RECOVERY, element: <Suspense fallback={<AuthSkeleton variant="recovery" />}><UserRecovery /></Suspense> },
            { path: ROUTES.AUTH.VERIFY_RECOVERY, element: <Suspense fallback={<AuthSkeleton variant="verification" />}><UserVerifyRecovery /></Suspense> },
            { path: ROUTES.AUTH.NEW_PASSWORD, element: <Suspense fallback={<AuthSkeleton variant="reset-password" />}><UserNewPassword /></Suspense> },
        ]
    },

    {
        element: <UserProtectedRoute />,
        errorElement: <GlobalError />,
        children: [
            { path: ROUTES.USER.CONFIGURATION, element: <Suspense fallback={<UserConfigSkeleton />}><UserConfiguration /></Suspense> },
            { path: ROUTES.USER.SESSIONS, element: <Suspense fallback={<UserConfigSkeleton />}><UserSessions /></Suspense> },
        ]
    },
    { path: '*', element: <Suspense fallback={<LandingSkeleton />}><NotFound /></Suspense>, errorElement: <GlobalError /> }
]);
