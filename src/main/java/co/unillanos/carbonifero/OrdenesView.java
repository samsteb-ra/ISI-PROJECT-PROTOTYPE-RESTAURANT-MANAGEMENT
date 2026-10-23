package co.unillanos.carbonifero;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Módulo Ordenar platos por mesa (RF-04).
 * Es el prototipo original, ahora conectado a las recetas: la disponibilidad de cada plato
 * sale del inventario de ingredientes y al confirmar una orden se descuentan los ingredientes.
 */
public class OrdenesView extends BorderPane {

    private static final int TOTAL_MESAS = 8;

    private final Datos datos;
    private final String css;
    private final NumberFormat moneda = NumberFormat.getIntegerInstance(Locale.forLanguageTag("es-CO"));

    // Estado de las órdenes
    private final Map<Integer, List<LineaOrden>> ordenesAbiertas = new HashMap<>();
    private final Map<Integer, Integer> numeroOrden = new HashMap<>();
    private final List<LineaOrden> carrito = new ArrayList<>();
    private Integer mesaSeleccionada = null;
    private int consecutivo = 1;

    // Componentes que se actualizan
    private final ToggleGroup grupoMesas = new ToggleGroup();
    private final Map<Integer, ToggleButton> botonesMesa = new HashMap<>();
    private final Map<Item, Button> botonesAgregar = new HashMap<>();
    private final Map<Item, Label> etiquetasDisponibilidad = new HashMap<>();
    private FlowPane flowPlatos;
    private VBox listaCarrito;
    private Label lblMesa;
    private Label lblOrdenAbierta;
    private VBox cuentaAbierta;
    private VBox listaCuenta;
    private Label lblTotalCuenta;
    private Button btnDevolucion;
    private Label lblTotal;
    private Label lblAviso;
    private Button btnConfirmar;
    private Button btnCancelar;

    public OrdenesView(Datos datos, String css) {
        this.datos = datos;
        this.css = css;
        setLeft(crearPanelMesas());
        setCenter(crearPanelMenu());
        setRight(crearPanelOrden());
        refrescar();
    }

    /** Se llama al entrar al módulo: toma los cambios hechos en recetas e inventario. */
    public void alActivar() {
        reconstruirMenu();
        refrescar();
    }

    // ------------------------------------------------------------------
    // Construcción de la interfaz
    // ------------------------------------------------------------------
    private VBox crearPanelMesas() {
        Label titulo = crearTitulo("MESAS");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        for (int i = 1; i <= TOTAL_MESAS; i++) {
            ToggleButton b = new ToggleButton("Mesa " + i);
            b.setUserData(i);
            b.setToggleGroup(grupoMesas);
            b.getStyleClass().add("mesa");
            b.setPrefSize(110, 60);
            botonesMesa.put(i, b);
            grid.add(b, (i - 1) % 2, (i - 1) / 2);
        }

        // Evita que la mesa quede sin selección al volver a hacer clic
        grupoMesas.selectedToggleProperty().addListener((obs, anterior, nuevo) -> {
            if (nuevo == null) {
                if (anterior != null) {
                    anterior.setSelected(true);
                }
                return;
            }
            mesaSeleccionada = (Integer) nuevo.getUserData();
            limpiarAviso();
            actualizarEtiquetaMesa();
        });

        VBox leyenda = new VBox(6,
                crearLeyenda("leyenda-libre", "Libre"),
                crearLeyenda("leyenda-ocupada", "Ocupada"));
        leyenda.setPadding(new Insets(10, 0, 0, 0));

        VBox panel = new VBox(14, titulo, grid, leyenda);
        panel.setPadding(new Insets(20));
        panel.setPrefWidth(270);
        panel.getStyleClass().add("panel-lateral");
        return panel;
    }

    private HBox crearLeyenda(String claseColor, String texto) {
        Region cuadro = new Region();
        cuadro.setMinSize(16, 16);
        cuadro.setMaxSize(16, 16);
        cuadro.getStyleClass().add(claseColor);
        Label l = new Label(texto);
        l.getStyleClass().add("leyenda-texto");
        HBox h = new HBox(8, cuadro, l);
        h.setAlignment(Pos.CENTER_LEFT);
        return h;
    }

    private ScrollPane crearPanelMenu() {
        flowPlatos = new FlowPane(16, 16);
        FlowPane flowAdicionales = new FlowPane(16, 16);
        for (Item a : datos.getAdicionales()) {
            flowAdicionales.getChildren().add(crearTarjeta(a));
        }
        reconstruirMenu();

        VBox contenido = new VBox(16,
                crearTitulo("PLATOS"), flowPlatos,
                crearTitulo("ADICIONALES"), flowAdicionales);
        contenido.setPadding(new Insets(24));

        ScrollPane scroll = new ScrollPane(contenido);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("scroll-menu");
        return scroll;
    }

    /** Vuelve a dibujar las tarjetas de los platos (por si se crearon platos nuevos en recetas). */
    private void reconstruirMenu() {
        for (Item p : datos.getPlatos()) {
            botonesAgregar.remove(p);
            etiquetasDisponibilidad.remove(p);
        }
        flowPlatos.getChildren().clear();
        for (Item p : datos.getPlatos()) {
            flowPlatos.getChildren().add(crearTarjeta(p));
        }
    }

    private VBox crearTarjeta(Item item) {
        boolean esPlato = item.getTipo() == Item.Tipo.PLATO;

        Label nombre = new Label(item.getNombre());
        nombre.getStyleClass().add("tarjeta-nombre");
        nombre.setWrapText(true);

        Label precio = new Label("$ " + moneda.format(item.getPrecio()));
        precio.getStyleClass().add("tarjeta-precio");

        Button agregar = new Button("Agregar");
        agregar.getStyleClass().add("btn-rojo");
        agregar.setMaxWidth(Double.MAX_VALUE);
        agregar.setOnAction(e -> agregar(item));
        botonesAgregar.put(item, agregar);

        VBox tarjeta = new VBox(8);
        tarjeta.getStyleClass().add("tarjeta");
        tarjeta.setPrefWidth(esPlato ? 250 : 170);
        tarjeta.getChildren().add(nombre);

        if (esPlato) {
            Label desc = new Label(item.getDescripcion());
            desc.getStyleClass().add("tarjeta-desc");
            desc.setWrapText(true);
            Label disp = new Label();
            etiquetasDisponibilidad.put(item, disp);
            tarjeta.getChildren().addAll(desc, precio, disp, agregar);
        } else {
            tarjeta.getChildren().addAll(precio, agregar);
        }
        return tarjeta;
    }

    private VBox crearPanelOrden() {
        Label titulo = crearTitulo("ORDEN ACTUAL");

        lblMesa = new Label();
        lblMesa.getStyleClass().add("orden-mesa");

        lblOrdenAbierta = new Label();
        lblOrdenAbierta.getStyleClass().add("orden-abierta");
        lblOrdenAbierta.setWrapText(true);

        // Cuenta abierta de la mesa: platos ya pedidos + botón de devoluciones
        Label tituloCuenta = new Label("CUENTA ABIERTA");
        tituloCuenta.getStyleClass().add("cuenta-titulo");

        listaCuenta = new VBox(6);
        ScrollPane scrollCuenta = new ScrollPane(listaCuenta);
        scrollCuenta.setFitToWidth(true);
        scrollCuenta.setPrefViewportHeight(110);
        scrollCuenta.setMaxHeight(130);
        scrollCuenta.getStyleClass().add("scroll-orden");

        Label textoTotalCuenta = new Label("Total cuenta");
        textoTotalCuenta.getStyleClass().add("cuenta-total-texto");
        lblTotalCuenta = new Label("$ 0");
        lblTotalCuenta.getStyleClass().add("cuenta-total-valor");
        Region espacioCuenta = new Region();
        HBox.setHgrow(espacioCuenta, Priority.ALWAYS);
        HBox filaTotalCuenta = new HBox(textoTotalCuenta, espacioCuenta, lblTotalCuenta);
        filaTotalCuenta.setAlignment(Pos.CENTER_LEFT);

        // Por ahora no hace nada: a futuro gestionará las devoluciones (RF-05)
        btnDevolucion = new Button("Gestionar devolución");
        btnDevolucion.getStyleClass().add("btn-borde");
        btnDevolucion.setMaxWidth(Double.MAX_VALUE);
        btnDevolucion.setOnAction(e -> { });

        cuentaAbierta = new VBox(8, tituloCuenta, scrollCuenta, filaTotalCuenta, lblOrdenAbierta, btnDevolucion);
        cuentaAbierta.getStyleClass().add("cuenta-abierta");
        cuentaAbierta.setVisible(false);
        cuentaAbierta.setManaged(false);

        listaCarrito = new VBox(8);
        ScrollPane scroll = new ScrollPane(listaCarrito);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("scroll-orden");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        Label textoTotal = new Label("TOTAL");
        textoTotal.getStyleClass().add("total-texto");
        lblTotal = new Label("$ 0");
        lblTotal.getStyleClass().add("total-valor");
        Region espacio = new Region();
        HBox.setHgrow(espacio, Priority.ALWAYS);
        HBox filaTotal = new HBox(textoTotal, espacio, lblTotal);
        filaTotal.setAlignment(Pos.CENTER_LEFT);
        filaTotal.getStyleClass().add("fila-total");

        lblAviso = new Label();
        lblAviso.getStyleClass().add("aviso");
        lblAviso.setWrapText(true);
        lblAviso.setVisible(false);
        lblAviso.setManaged(false);

        btnConfirmar = new Button("Confirmar orden");
        btnConfirmar.getStyleClass().add("btn-rojo");
        btnConfirmar.setMaxWidth(Double.MAX_VALUE);
        btnConfirmar.setOnAction(e -> confirmar());

        btnCancelar = new Button("Cancelar orden");
        btnCancelar.getStyleClass().add("btn-borde");
        btnCancelar.setMaxWidth(Double.MAX_VALUE);
        btnCancelar.setOnAction(e -> cancelar());

        VBox panel = new VBox(12, titulo, lblMesa, cuentaAbierta, scroll, filaTotal, lblAviso,
                btnConfirmar, btnCancelar);
        panel.setPadding(new Insets(20));
        panel.setPrefWidth(360);
        panel.getStyleClass().add("panel-orden");
        return panel;
    }

    private Label crearTitulo(String texto) {
        Label l = new Label(texto);
        l.getStyleClass().add("titulo-seccion");
        return l;
    }

    private HBox crearFila(LineaOrden linea) {
        Label nombre = new Label(linea.getItem().getNombre());
        nombre.getStyleClass().add("fila-nombre");
        nombre.setWrapText(true);
        nombre.setPrefWidth(120);

        Button menos = new Button("-");
        menos.getStyleClass().add("btn-mini");
        menos.setOnAction(e -> quitar(linea));

        Label cantidad = new Label(String.valueOf(linea.getCantidad()));
        cantidad.getStyleClass().add("fila-cantidad");

        Button mas = new Button("+");
        mas.getStyleClass().add("btn-mini");
        mas.setOnAction(e -> agregar(linea.getItem()));

        Label subtotal = new Label("$ " + moneda.format(linea.getSubtotal()));
        subtotal.getStyleClass().add("fila-subtotal");

        Region espacio = new Region();
        HBox.setHgrow(espacio, Priority.ALWAYS);

        HBox fila = new HBox(8, nombre, espacio, menos, cantidad, mas, subtotal);
        fila.setAlignment(Pos.CENTER_LEFT);
        fila.getStyleClass().add("fila-orden");
        return fila;
    }

    // ------------------------------------------------------------------
    // Lógica del prototipo
    // ------------------------------------------------------------------

    /** Cuánto de un ingrediente consumen los platos que están en el carrito. */
    private double consumoCarrito(Ingrediente ingrediente) {
        double total = 0;
        for (LineaOrden l : carrito) {
            if (l.getItem().controlaStock()) {
                for (LineaReceta r : l.getItem().getReceta()) {
                    if (r.getIngrediente() == ingrediente) {
                        total += r.getCantidad() * l.getCantidad();
                    }
                }
            }
        }
        return total;
    }

    /** Porciones que todavía se pueden agregar de un plato, descontando lo que ya está en el carrito. */
    private int disponiblesConCarrito(Item plato) {
        if (plato.getReceta().isEmpty()) {
            return 0;
        }
        int minimo = Integer.MAX_VALUE;
        for (LineaReceta r : plato.getReceta()) {
            if (r.getCantidad() > 0) {
                double restante = r.getIngrediente().getStock() - consumoCarrito(r.getIngrediente());
                minimo = Math.min(minimo, (int) Math.floor(restante / r.getCantidad()));
            }
        }
        return minimo == Integer.MAX_VALUE ? 0 : Math.max(minimo, 0);
    }

    /** Nombre del primer ingrediente que no alcanza para toda la orden, o null si todo alcanza. */
    private String ingredienteFaltante() {
        for (LineaOrden l : carrito) {
            if (l.getItem().controlaStock()) {
                for (LineaReceta r : l.getItem().getReceta()) {
                    if (consumoCarrito(r.getIngrediente()) > r.getIngrediente().getStock()) {
                        return r.getIngrediente().getNombre();
                    }
                }
            }
        }
        return null;
    }

    private void agregar(Item item) {
        if (item.controlaStock() && disponiblesConCarrito(item) <= 0) {
            mostrarAviso("\"" + item.getNombre() + "\" no está disponible: no hay ingredientes suficientes.");
            return;
        }
        limpiarAviso();
        for (LineaOrden l : carrito) {
            if (l.getItem() == item) {
                l.setCantidad(l.getCantidad() + 1);
                refrescar();
                return;
            }
        }
        carrito.add(new LineaOrden(item, 1));
        refrescar();
    }

    private void quitar(LineaOrden linea) {
        limpiarAviso();
        if (linea.getCantidad() > 1) {
            linea.setCantidad(linea.getCantidad() - 1);
        } else {
            carrito.remove(linea);
        }
        refrescar();
    }

    private void cancelar() {
        carrito.clear();
        limpiarAviso();
        refrescar();
    }

    private void confirmar() {
        if (mesaSeleccionada == null) {
            mostrarAviso("Selecciona una mesa antes de confirmar la orden.");
            return;
        }
        if (carrito.isEmpty()) {
            mostrarAviso("Agrega al menos un plato a la orden.");
            return;
        }
        String faltante = ingredienteFaltante();
        if (faltante != null) {
            mostrarAviso("No hay suficiente \"" + faltante + "\" en el inventario para esta orden.");
            return;
        }

        int mesa = mesaSeleccionada;
        List<LineaOrden> abiertas = ordenesAbiertas.computeIfAbsent(mesa, k -> new ArrayList<>());
        if (!numeroOrden.containsKey(mesa)) {
            numeroOrden.put(mesa, consecutivo++);
        }

        StringBuilder comanda = new StringBuilder();
        StringBuilder alertas = new StringBuilder();
        int totalComanda = 0;

        for (LineaOrden l : carrito) {
            Item item = l.getItem();
            if (item.controlaStock()) {
                String errorConsumo = datos.consumirPorPreparacion(item, l.getCantidad());
                if (errorConsumo != null) {
                    mostrarAviso(errorConsumo);
                    return;
                }
            }
            LineaOrden existente = null;
            for (LineaOrden a : abiertas) {
                if (a.getItem() == item) {
                    existente = a;
                }
            }
            if (existente != null) {
                existente.setCantidad(existente.getCantidad() + l.getCantidad());
            } else {
                abiertas.add(new LineaOrden(item, l.getCantidad()));
            }
            comanda.append(l.getCantidad()).append(" x ").append(item.getNombre()).append("\n");
            totalComanda += l.getSubtotal();
        }

        // Alerta de stock mínimo de platos e ingredientes, ya con el inventario actualizado
        for (LineaOrden l : carrito) {
            Item item = l.getItem();
            if (item.controlaStock() && item.getPorciones() <= Datos.STOCK_MINIMO) {
                alertas.append("- ").append(item.getNombre())
                        .append(item.getPorciones() == 0 ? " (agotado)" : " (quedan " + item.getPorciones() + ")")
                        .append("\n");
            }
        }
        for (LineaOrden l : carrito) {
            if (!l.getItem().controlaStock()) {
                continue;
            }
            for (LineaReceta r : l.getItem().getReceta()) {
                Ingrediente ing = r.getIngrediente();
                if (ing.estaEnAlerta()) {
                    String lineaIng = "- " + ing.getNombre() + (ing.estaAgotado()
                            ? " (agotado)"
                            : " (quedan " + ing.getStock() + " " + ing.getUnidad() + ")");
                    if (alertas.indexOf(lineaIng) < 0) {
                        alertas.append(lineaIng).append("\n");
                    }
                }
            }
        }

        botonesMesa.get(mesa).getStyleClass().remove("mesa-ocupada");
        botonesMesa.get(mesa).getStyleClass().add("mesa-ocupada");

        String mensaje = comanda + "\nTotal de esta comanda: $ " + moneda.format(totalComanda)
                + "\nEl inventario fue actualizado.";
        if (alertas.length() > 0) {
            mensaje += "\n\nAlerta de stock mínimo:\n" + alertas;
        }

        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle("Orden registrada");
        alerta.setHeaderText("Orden #" + numeroOrden.get(mesa) + " registrada - Mesa " + mesa);
        alerta.setContentText(mensaje);
        alerta.getDialogPane().getStylesheets().add(css);
        alerta.showAndWait();

        carrito.clear();
        limpiarAviso();
        refrescar();
    }

    // ------------------------------------------------------------------
    // Actualización de la interfaz
    // ------------------------------------------------------------------
    private void refrescar() {
        listaCarrito.getChildren().clear();
        int total = 0;
        if (carrito.isEmpty()) {
            Label vacio = new Label("Aún no hay platos en la orden.");
            vacio.getStyleClass().add("vacio");
            listaCarrito.getChildren().add(vacio);
        }
        for (LineaOrden l : carrito) {
            total += l.getSubtotal();
            listaCarrito.getChildren().add(crearFila(l));
        }
        lblTotal.setText("$ " + moneda.format(total));
        btnConfirmar.setDisable(carrito.isEmpty());
        btnCancelar.setDisable(carrito.isEmpty());

        for (Item p : datos.getPlatos()) {
            int disponibles = disponiblesConCarrito(p);
            Label etiqueta = etiquetasDisponibilidad.get(p);
            etiqueta.getStyleClass().removeAll("disp-ok", "disp-agotado");
            if (disponibles <= 0) {
                etiqueta.setText("No disponible");
                etiqueta.getStyleClass().add("disp-agotado");
            } else {
                etiqueta.setText("Disponibles: " + disponibles);
                etiqueta.getStyleClass().add("disp-ok");
            }
            Button boton = botonesAgregar.get(p);
            boton.setDisable(disponibles <= 0);
            boton.setText(disponibles <= 0 ? "Agotado" : "Agregar");
        }
        actualizarEtiquetaMesa();
    }

    private void actualizarEtiquetaMesa() {
        if (mesaSeleccionada == null) {
            lblMesa.setText("Mesa: sin seleccionar");
            mostrarCuentaAbierta(false);
            return;
        }
        lblMesa.setText("Mesa " + mesaSeleccionada);
        List<LineaOrden> abiertas = ordenesAbiertas.get(mesaSeleccionada);
        if (abiertas == null || abiertas.isEmpty()) {
            mostrarCuentaAbierta(false);
            return;
        }

        // Cuenta ya abierta: platos pedidos antes en esta mesa
        listaCuenta.getChildren().clear();
        int totalAbierto = 0;
        for (LineaOrden l : abiertas) {
            totalAbierto += l.getSubtotal();
            listaCuenta.getChildren().add(crearFilaCuenta(l));
        }
        lblTotalCuenta.setText("$ " + moneda.format(totalAbierto));
        lblOrdenAbierta.setText("Los nuevos platos se agregarán a esta misma cuenta.");
        mostrarCuentaAbierta(true);
    }

    private HBox crearFilaCuenta(LineaOrden linea) {
        Label detalle = new Label(linea.getCantidad() + " x " + linea.getItem().getNombre());
        detalle.getStyleClass().add("cuenta-fila");
        detalle.setWrapText(true);
        Label subtotal = new Label("$ " + moneda.format(linea.getSubtotal()));
        subtotal.getStyleClass().add("fila-subtotal");
        Region espacio = new Region();
        HBox.setHgrow(espacio, Priority.ALWAYS);
        HBox fila = new HBox(8, detalle, espacio, subtotal);
        fila.setAlignment(Pos.CENTER_LEFT);
        return fila;
    }

    private void mostrarCuentaAbierta(boolean visible) {
        cuentaAbierta.setVisible(visible);
        cuentaAbierta.setManaged(visible);
    }

    private void mostrarAviso(String texto) {
        lblAviso.setText(texto);
        lblAviso.setVisible(true);
        lblAviso.setManaged(true);
    }

    private void limpiarAviso() {
        if (lblAviso != null) {
            lblAviso.setVisible(false);
            lblAviso.setManaged(false);
        }
    }
}
