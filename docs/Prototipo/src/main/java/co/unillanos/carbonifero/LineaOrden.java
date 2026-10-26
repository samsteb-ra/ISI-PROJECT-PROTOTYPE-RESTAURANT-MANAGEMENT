package co.unillanos.carbonifero;

/** Renglón de una orden: un item con su cantidad. */
public class LineaOrden {

    private final Item item;
    private int cantidad;

    public LineaOrden(Item item, int cantidad) {
        this.item = item;
        this.cantidad = cantidad;
    }

    public Item getItem() { return item; }
    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
    public int getSubtotal() { return item.getPrecio() * cantidad; }
}
