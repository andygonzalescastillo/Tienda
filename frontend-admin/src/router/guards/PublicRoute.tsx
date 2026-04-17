import { Navigate, Outlet } from 'react-router-dom';
import { ROUTES } from '@/router/routes';
import { useEstaAutenticado } from '@/auth/store/authStore';

export const PublicRoute = () =>
    useEstaAutenticado() ? <Navigate to={ROUTES.ADMIN.DASHBOARD} replace /> : <Outlet />;
