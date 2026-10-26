package co.unillanos.carbonifero;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Datos de ejemplo compartidos por los módulos (en memoria, sin base de datos).
 * Concentra las reglas de inventario (RF-02 / RF-03): compras, ajustes,
 * altas y el consumo automático al confirmar una orden.
 */
public class Datos {

    /** Umbral de porciones bajas de un plato (alerta en órdenes y recetas). */
    public static final int STOCK_MINIMO = 2;
    public static final String USUARIO_ADMIN = "Administrador";

    private final List<Ingrediente> inventario = new ArrayList<>();
    private final List<Item> platos = new ArrayList<>();
    private final List<Item> adicionales = new ArrayList<>();
    private final List<MovimientoInventario> movimientos = new ArrayList<>();

    public Datos() {
        Ingrediente carne = new Ingrediente("Carne de res", "g", 5000, 800);
        Ingrediente costillas = new Ingrediente("Costillas de cerdo", "g", 1200, 400);
        Ingrediente pechuga = new Ingrediente("Pechuga de pollo", "g", 2000, 500);
        Ingrediente yuca = new Ingrediente("Yuca", "g", 1800, 400);
        Ingrediente papa = new Ingrediente("Papa", "g", 6000, 800);
        Ingrediente ensalada = new Ingrediente("Ensalada mixta", "g", 2000, 300);
        Ingrediente salsaBbq = new Ingrediente("Salsa BBQ", "ml", 1500, 200);
        Ingrediente arroz = new Ingrediente("Arroz", "g", 5000, 500);
        Ingrediente pan = new Ingrediente("Pan de hamburguesa", "unidad", 20, 8);
        Ingrediente queso = new Ingrediente("Queso", "g", 240, 80);
        Ingrediente tocineta = new Ingrediente("Tocineta", "g", 500, 100);
        Ingrediente aceite = new Ingrediente("Aceite", "ml", 3000, 500);
        Ingrediente sal = new Ingrediente("Sal", "g", 10, 50); // queda bajo el mínimo a propósito
        inventario.addAll(List.of(carne, costillas, pechuga, yuca, papa, ensalada, salsaBbq,
                arroz, pan, queso, tocineta, aceite, sal));

        Item llanera = new Item("Carne a la llanera", "Carne asada al carbón con yuca, papa y ensalada.", 38000, Item.Tipo.PLATO);
        llanera.getReceta().addAll(List.of(new LineaReceta(carne, 300), new LineaReceta(yuca, 150),
                new LineaReceta(papa, 150), new LineaReceta(ensalada, 80)));

        Item costillasBbq = new Item("Costillas BBQ", "Costillas ahumadas con salsa BBQ de la casa.", 42000, Item.Tipo.PLATO);
        costillasBbq.getReceta().addAll(List.of(new LineaReceta(costillas, 400), new LineaReceta(salsaBbq, 60)));

        Item pechugaPlato = new Item("Pechuga a la parrilla", "Pechuga marinada con arroz y ensalada fresca.", 32000, Item.Tipo.PLATO);
        pechugaPlato.getReceta().addAll(List.of(new LineaReceta(pechuga, 250), new LineaReceta(arroz, 120),
                new LineaReceta(ensalada, 80)));

        Item hamburguesa = new Item("Hamburguesa Carbonífero", "Carne de res, queso, tocineta y papas a la francesa.", 28000, Item.Tipo.PLATO);
        hamburguesa.getReceta().addAll(List.of(new LineaReceta(carne, 180), new LineaReceta(pan, 1),
                new LineaReceta(queso, 40), new LineaReceta(tocineta, 30), new LineaReceta(papa, 200)));

        platos.addAll(List.of(llanera, costillasBbq, pechugaPlato, hamburguesa));

        adicionales.add(new Item("Patacón", "", 6000, Item.Tipo.ADICIONAL));
        adicionales.add(new Item("Yuca frita", "", 5000, Item.Tipo.ADICIONAL));
        adicionales.add(new Item("Papa criolla", "", 5000, Item.Tipo.ADICIONAL));
        adicionales.add(new Item("Aguacate", "", 4000, Item.Tipo.ADICIONAL));
        adicionales.add(new Item("Huevo frito", "", 3000, Item.Tipo.ADICIONAL));
    }

    public List<Ingrediente> getInventario() { return inventario; }
    public List<Item> getPlatos() { return platos; }
    public List<Item> getAdicionales() { return adicionales; }
    public List<MovimientoInventario> getMovimientos() { return movimientos; }

    /**
     * Compra: suma existencia. La cantidad debe ser mayor a cero.
     * @return mensaje de error, o null si se registró.
     */
    public String registrarCompra(Ingrediente ingrediente, double cantidad, String usuario) {
        if (ingrediente == null) {
            return "Selecciona un ingrediente.";
        }
        String error = validarCantidadPositiva(cantidad);
        if (error != null) {
            return error;
        }
        aplicarMovimiento(ingrediente, MovimientoInventario.Tipo.COMPRA, cantidad,
                "Compra de insumos", usuario);
        return null;
    }

    /**
     * Ajuste manual. {@code delta} puede ser positivo o negativo; no deja el stock en negativo.
     * El motivo es obligatorio.
     */
    public String ajustarExistencias(Ingrediente ingrediente, double delta, String motivo, String usuario) {
        if (ingrediente == null) {
            return "Selecciona un ingrediente.";
        }
        if (delta == 0 || Double.isNaN(delta) || Double.isInfinite(delta)) {
            return "Ingresa una cantidad de ajuste distinta de cero.";
        }
        if (motivo == null || motivo.trim().isEmpty()) {
            return "Indica el motivo del ajuste manual.";
        }
        if (ingrediente.getStock() + delta < 0) {
            return "El ajuste dejaría la existencia en negativo.";
        }
        aplicarMovimiento(ingrediente, MovimientoInventario.Tipo.AJUSTE, delta, motivo.trim(), usuario);
        return null;
    }

    /**
     * Alta de un ingrediente nuevo. La existencia inicial no puede ser negativa.
     */
    public String crearIngrediente(String nombre, String unidad, double stockInicial,
                                   double stockMinimo, String usuario) {
        if (nombre == null || nombre.trim().isEmpty()) {
            return "Escribe el nombre del ingrediente.";
        }
        if (unidad == null || unidad.trim().isEmpty()) {
            return "Selecciona la unidad de medida.";
        }
        if (Double.isNaN(stockInicial) || Double.isInfinite(stockInicial) || stockInicial < 0) {
            return "La existencia inicial no puede ser negativa.";
        }
        if (Double.isNaN(stockMinimo) || Double.isInfinite(stockMinimo) || stockMinimo < 0) {
            return "El stock mínimo no puede ser negativo.";
        }
        String nombreLimpio = nombre.trim();
        for (Ingrediente existente : inventario) {
            if (existente.getNombre().equalsIgnoreCase(nombreLimpio)) {
                return "Ya existe un ingrediente llamado \"" + existente.getNombre() + "\".";
            }
        }
        Ingrediente nuevo = new Ingrediente(nombreLimpio, unidad.trim(), stockInicial, stockMinimo);
        inventario.add(nuevo);
        movimientos.add(0, new MovimientoInventario(LocalDateTime.now(), usuario, nuevo,
                MovimientoInventario.Tipo.ALTA, stockInicial, 0, stockInicial, "Alta de ingrediente"));
        return null;
    }

    /**
     * Consumo automático al preparar un plato (confirmación de orden).
     * Recalcula de inmediato las porciones posibles de los platos afectados.
     */
    public String consumirPorPreparacion(Item plato, int porciones) {
        if (plato == null || !plato.controlaStock() || porciones <= 0) {
            return null;
        }
        for (LineaReceta linea : plato.getReceta()) {
            double requerido = linea.getCantidad() * porciones;
            if (linea.getIngrediente().getStock() < requerido) {
                return "No hay suficiente \"" + linea.getIngrediente().getNombre()
                        + "\" en el inventario para esta orden.";
            }
        }
        for (LineaReceta linea : plato.getReceta()) {
            double requerido = linea.getCantidad() * porciones;
            aplicarMovimiento(linea.getIngrediente(), MovimientoInventario.Tipo.CONSUMO, -requerido,
                    porciones + " x " + plato.getNombre(), USUARIO_ADMIN);
        }
        return null;
    }

    /** Platos cuya receta usa el ingrediente (para alertas de disponibilidad). */
    public List<Item> platosQueUsan(Ingrediente ingrediente) {
        List<Item> afectados = new ArrayList<>();
        for (Item plato : platos) {
            for (LineaReceta linea : plato.getReceta()) {
                if (linea.getIngrediente() == ingrediente) {
                    afectados.add(plato);
                    break;
                }
            }
        }
        return afectados;
    }

    public List<Ingrediente> ingredientesEnAlerta() {
        List<Ingrediente> alerta = new ArrayList<>();
        for (Ingrediente ing : inventario) {
            if (ing.estaEnAlerta()) {
                alerta.add(ing);
            }
        }
        return alerta;
    }

    private String validarCantidadPositiva(double cantidad) {
        if (!(cantidad > 0) || Double.isInfinite(cantidad) || Double.isNaN(cantidad)) {
            return "Ingresa una cantidad válida, mayor a cero.";
        }
        return null;
    }

    private void aplicarMovimiento(Ingrediente ingrediente, MovimientoInventario.Tipo tipo,
                                   double delta, String motivo, String usuario) {
        double anterior = ingrediente.getStock();
        double nueva = anterior + delta;
        ingrediente.setStock(nueva);
        movimientos.add(0, new MovimientoInventario(LocalDateTime.now(), usuario, ingrediente,
                tipo, delta, anterior, nueva, motivo));
    }
}
