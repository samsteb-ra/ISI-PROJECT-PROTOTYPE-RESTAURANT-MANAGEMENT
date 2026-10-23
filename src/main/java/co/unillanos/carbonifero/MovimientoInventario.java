package co.unillanos.carbonifero;

import java.time.LocalDateTime;

/**
 * Registro de un cambio de existencia (compra, ajuste, consumo o alta).
 * Guarda fecha, hora, usuario y el efecto sobre el stock (CU-02, paso 6).
 */
public class MovimientoInventario {

    public enum Tipo { COMPRA, AJUSTE, CONSUMO, ALTA }

    private final LocalDateTime fechaHora;
    private final String usuario;
    private final Ingrediente ingrediente;
    private final Tipo tipo;
    private final double cantidad; // positiva si entra, negativa si sale
    private final double existenciaAnterior;
    private final double existenciaNueva;
    private final String motivo;

    public MovimientoInventario(LocalDateTime fechaHora, String usuario, Ingrediente ingrediente,
                                Tipo tipo, double cantidad, double existenciaAnterior,
                                double existenciaNueva, String motivo) {
        this.fechaHora = fechaHora;
        this.usuario = usuario;
        this.ingrediente = ingrediente;
        this.tipo = tipo;
        this.cantidad = cantidad;
        this.existenciaAnterior = existenciaAnterior;
        this.existenciaNueva = existenciaNueva;
        this.motivo = motivo;
    }

    public LocalDateTime getFechaHora() { return fechaHora; }
    public String getUsuario() { return usuario; }
    public Ingrediente getIngrediente() { return ingrediente; }
    public Tipo getTipo() { return tipo; }
    public double getCantidad() { return cantidad; }
    public double getExistenciaAnterior() { return existenciaAnterior; }
    public double getExistenciaNueva() { return existenciaNueva; }
    public String getMotivo() { return motivo; }
}
