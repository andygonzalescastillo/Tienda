import { ShoppingBag } from 'lucide-react';
import { LandingHeader } from '../components/LandingHeader';
import { ProductCard } from '../components/ProductCard';

export const LandingPage = () => {
    const productos = Array.from({ length: 6 }, (_, i) => ({
        id: i + 1,
        precio: (109.99 + i * 10).toFixed(2),
        nombre: `Producto Premium ${i + 1}`,
        descripcion: 'Un producto de alta calidad diseñado para satisfacer todas tus necesidades con el mejor estilo.'
    }));

    return (
        <div className="min-h-screen bg-background flex flex-col">
            <LandingHeader />

            <main className="flex-1 max-w-7xl mx-auto px-4 py-12 md:py-20 w-full">
                <section className="text-center mb-16 space-y-4">
                    <h2 className="text-4xl font-extrabold tracking-tight lg:text-5xl text-foreground">
                        Bienvenido a Mi Tienda
                    </h2>
                    <p className="text-xl text-muted-foreground max-w-2xl mx-auto">
                        Los mejores productos seleccionados especialmente para ti, al mejor precio del mercado.
                    </p>
                </section>

                <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-8">
                    {productos.map(({ id, ...producto }) => (
                        <ProductCard key={id} {...producto} />
                    ))}
                </div>
            </main>

            <footer className="border-t bg-card py-8 md:py-12">
                <div className="max-w-7xl mx-auto px-4 flex flex-col items-center gap-4">
                    <div className="flex items-center gap-2 grayscale opacity-50">
                        <ShoppingBag className="h-5 w-5" />
                        <span className="font-semibold">Mi Tienda</span>
                    </div>
                    <p className="text-center text-sm text-muted-foreground leading-loose">
                        &copy; {new Date().getFullYear()} Mi Tienda. Todos los derechos reservados. <br className="md:hidden" />
                        Hecho con pasión por el equipo de diseño.
                    </p>
                </div>
            </footer>
        </div>
    );
}