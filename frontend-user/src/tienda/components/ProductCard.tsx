import { Button } from '@/components/ui/button';
import { Card, CardContent, CardFooter } from '@/components/ui/card';
import { Package } from 'lucide-react';

interface ProductCardProps {
    nombre: string;
    descripcion: string;
    precio: string;
}

export const ProductCard = ({ nombre, descripcion, precio }: ProductCardProps) => (
    <Card className="overflow-hidden border-border bg-card hover:shadow-lg transition-all duration-300">
        <CardContent className="p-0">
            <div className="bg-muted aspect-video flex flex-col items-center justify-center text-muted-foreground/40 group">
                <Package className="h-12 w-12 mb-2 transition-transform group-hover:scale-110" />
                <span className="text-xs font-medium uppercase tracking-wider">Imagen del Producto</span>
            </div>
            <div className="p-6 space-y-2">
                <h3 className="text-lg font-bold text-foreground leading-none">{nombre}</h3>
                <p className="text-sm text-muted-foreground line-clamp-2">{descripcion}</p>
            </div>
        </CardContent>
        <CardFooter className="flex justify-between items-center p-6 pt-4">
            <span className="text-2xl font-bold text-foreground">${precio}</span>
            <Button size="sm" variant="default">
                Agregar
            </Button>
        </CardFooter>
    </Card>
);
