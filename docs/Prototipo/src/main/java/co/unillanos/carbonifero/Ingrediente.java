package co.unillanos.carbonifero;

/**
 * Insumo del inventario (RF-02 / RF-03).
 * La existencia se actualiza con compras, ajustes manuales y el consumo
 * automático de cada plato preparado.
 */
public class Ingrediente {

    private final String nombre;
    private final String unidad; // g, ml, unidad
    private double stock;
    private double stockMinimo;

    public Ingrediente(String nombre, String unidad, double stock, double stockMinimo) {
        this.nombre = nombre;
        this.unidad = unidad;
        this.stock = stock;
        this.stockMinimo = stockMinimo;
    }

    /** Descuenta del inventario (lo usa la confirmación de una orden). */
    public void descontar(double cantidad) {
        stock -= cantidad;
        if (stock < 0) {
            stock = 0;
        }
    }

    public void setStock(double stock) {
        this.stock = stock;
    }

    public void setStockMinimo(double stockMinimo) {
        this.stockMinimo = stockMinimo;
    }

    public boolean estaAgotado() {
        return stock <= 0;
    }

    /** RF-03: existencia menor o igual al umbral configurado. */
    public boolean estaEnAlerta() {
        return stock <= stockMinimo;
    }

    public String getNombre() { return nombre; }
    public String getUnidad() { return unidad; }
    public double getStock() { return stock; }
    public double getStockMinimo() { return stockMinimo; }

    /** Texto que se muestra en la lista desplegable. */
    @Override
    public String toString() {
        return nombre + " (" + unidad + ")";
    }
}
