
package co.unillanos.carbonifero;

import java.time.LocalDateTime;

/**
 * Representa el pago de una cuenta cerrada.
 */
public class Pago {

    public enum Metodo {
        EFECTIVO("Efectivo"),
        TARJETA("Tarjeta"),
        TRANSFERENCIA("Transferencia");

        private final String nombre;

        Metodo(String nombre) {
            this.nombre = nombre;
        }

        @Override
        public String toString() {
            return nombre;
        }
    }

    private final int numeroOrden;
    private final int mesa;
    private final int total;
    private final Metodo metodo;
    private final LocalDateTime fecha;

    public Pago(int numeroOrden, int mesa, int total, Metodo metodo) {
        this.numeroOrden = numeroOrden;
        this.mesa = mesa;
        this.total = total;
        this.metodo = metodo;
        this.fecha = LocalDateTime.now();
    }

    public int getNumeroOrden() {
        return numeroOrden;
    }

    public int getMesa() {
        return mesa;
    }

    public int getTotal() {
        return total;
    }

    public Metodo getMetodo() {
        return metodo;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }
}
