package co.unillanos.carbonifero;

/** Ingrediente del inventario (datos de ejemplo; el inventario real lo gestiona otro módulo). */
public class Ingrediente {

    private final String nombre;
    private final String unidad; // g, ml, unidad
    private double stock;

    public Ingrediente(String nombre, String unidad, double stock) {
        this.nombre = nombre;
        this.unidad = unidad;
        this.stock = stock;
    }

    /** Descuenta del inventario (lo usa la confirmación de una orden). */
    public void descontar(double cantidad) {
        stock -= cantidad;
    }

    public String getNombre() { return nombre; }
    public String getUnidad() { return unidad; }
    public double getStock() { return stock; }

    /** Texto que se muestra en la lista desplegable. */
    @Override
    public String toString() {
        return nombre + " (" + unidad + ")";
    }
}
