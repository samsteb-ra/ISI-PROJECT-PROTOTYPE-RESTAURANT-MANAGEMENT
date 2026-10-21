package co.unillanos.carbonifero;

/** Renglón de una receta: un ingrediente con la cantidad que lleva cada porción del plato. */
public class LineaReceta {

    private final Ingrediente ingrediente;
    private double cantidad;

    public LineaReceta(Ingrediente ingrediente, double cantidad) {
        this.ingrediente = ingrediente;
        this.cantidad = cantidad;
    }

    public Ingrediente getIngrediente() { return ingrediente; }
    public double getCantidad() { return cantidad; }
    public void setCantidad(double cantidad) { this.cantidad = cantidad; }

    /** Indica si el inventario alcanza para al menos una porción. */
    public boolean alcanza() {
        return ingrediente.getStock() >= cantidad;
    }
}
