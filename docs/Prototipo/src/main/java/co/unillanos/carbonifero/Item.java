package co.unillanos.carbonifero;

import java.util.ArrayList;
import java.util.List;

/**
 * Elemento del menú: un plato o un adicional.
 * Los platos tienen receta (ingredientes por porción) y su disponibilidad
 * se calcula con el inventario; los adicionales no controlan stock.
 */
public class Item {

    public enum Tipo { PLATO, ADICIONAL }

    private String nombre;
    private String descripcion;
    private int precio;
    private final Tipo tipo;
    private final List<LineaReceta> receta = new ArrayList<>(); // solo platos

    public Item(String nombre, String descripcion, int precio, Tipo tipo) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.tipo = tipo;
    }

    public boolean controlaStock() {
        return tipo == Tipo.PLATO;
    }

    /** Porciones que se pueden preparar con el inventario actual (-1 si no controla stock). */
    public int getPorciones() {
        return controlaStock() ? calcularPorciones(receta) : -1;
    }

    /** Descuenta del inventario los ingredientes de las porciones indicadas. */
    public void descontar(int cantidad) {
        if (!controlaStock()) {
            return;
        }
        for (LineaReceta r : receta) {
            r.getIngrediente().descontar(r.getCantidad() * cantidad);
        }
    }

    public static int calcularPorciones(List<LineaReceta> lineas) {
        if (lineas.isEmpty()) {
            return 0;
        }
        int minimo = Integer.MAX_VALUE;
        for (LineaReceta l : lineas) {
            if (l.getCantidad() > 0) {
                int posibles = (int) Math.floor(l.getIngrediente().getStock() / l.getCantidad());
                minimo = Math.min(minimo, posibles);
            }
        }
        return minimo == Integer.MAX_VALUE ? 0 : Math.max(minimo, 0);
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public int getPrecio() { return precio; }
    public void setPrecio(int precio) { this.precio = precio; }
    public Tipo getTipo() { return tipo; }
    public List<LineaReceta> getReceta() { return receta; }
}
