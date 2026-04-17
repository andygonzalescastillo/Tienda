import { Navigate, Outlet } from 'react-router-dom';
import { ROUTES } from '@/router/routes';
import { useEstaAutenticado } from '@/auth/store/authStore';

export const UserProtectedRoute = () =>
    useEstaAutenticado() ? <Outlet /> : <Navigate to={ROUTES.HOME} replace />;
