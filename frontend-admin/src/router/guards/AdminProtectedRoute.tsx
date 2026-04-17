import { Navigate, Outlet } from 'react-router-dom';
import { ROUTES } from '@/router/routes';
import { useEstaAutenticado } from '@/auth/store/authStore';

export const AdminProtectedRoute = () =>
    useEstaAutenticado() ? <Outlet /> : <Navigate to={ROUTES.AUTH.LOGIN} replace />;
