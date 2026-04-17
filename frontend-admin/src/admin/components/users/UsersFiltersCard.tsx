import { Card, CardContent } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Search } from 'lucide-react';
import type { UserFilters } from '@/admin/types/userTypes';

interface Props {
    filters: UserFilters;
    searchInput: string;
    setSearchInput: (value: string) => void;
    handleFilterChange: (key: 'rol' | 'estado', value: string) => void;
}

export const UsersFiltersCard = ({
    filters,
    searchInput,
    setSearchInput,
    handleFilterChange
}: Props) => {
    return (
        <Card className="mb-6">
            <CardContent className="pt-6">
                <div className="flex flex-col sm:flex-row gap-3">
                    <div className="relative flex-1">
                        <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground" />
                        <Input
                            placeholder="Buscar por email o nombre..."
                            value={searchInput}
                            onChange={(e) => setSearchInput(e.target.value)}
                            className="pl-10"
                        />
                    </div>

                    <Select
                        value={filters.rol || "ALL"}
                        onValueChange={(val: string) => handleFilterChange('rol', val === "ALL" ? "" : val)}
                    >
                        <SelectTrigger className="w-full sm:w-45">
                            <SelectValue placeholder="Todos los roles" />
                        </SelectTrigger>
                        <SelectContent>
                            <SelectItem value="ALL">Todos los roles</SelectItem>
                            <SelectItem value="USER">Usuario</SelectItem>
                            <SelectItem value="ADMIN">Administrador</SelectItem>
                        </SelectContent>
                    </Select>

                    <Select
                        value={filters.estado || "ALL"}
                        onValueChange={(val: string) => handleFilterChange('estado', val === "ALL" ? "" : val)}
                    >
                        <SelectTrigger className="w-full sm:w-45">
                            <SelectValue placeholder="Todos los estados" />
                        </SelectTrigger>
                        <SelectContent>
                            <SelectItem value="ALL">Todos los estados</SelectItem>
                            <SelectItem value="true">Activo</SelectItem>
                            <SelectItem value="false">Inactivo</SelectItem>
                        </SelectContent>
                    </Select>
                </div>
            </CardContent>
        </Card>
    );
};

