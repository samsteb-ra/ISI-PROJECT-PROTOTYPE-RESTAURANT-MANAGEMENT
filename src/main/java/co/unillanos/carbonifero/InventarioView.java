package co.unillanos.carbonifero;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Módulo Gestionar inventario de ingredientes (CU-02 / RF-02 / RF-03).
 * Permite al administrador ver existencias, registrar compras, ajustar stock
 * y crear insumos. Las alertas de stock mínimo se reflejan en los platos.
 */
public class InventarioView extends BorderPane {

    private enum Accion { COMPRA, AJUSTE, NUEVO }

    private final Datos datos;
    private final String css;
    private final NumberFormat numero = NumberFormat.getNumberInstance(Locale.forLanguageTag("es-CO"));
    private final DateTimeFormatter fechaHora = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private Accion accion = Accion.COMPRA;
    private Ingrediente seleccionado = null;
    private boolean actualizandoLista = false;

    private final ToggleGroup grupoIngredientes = new ToggleGroup();
    private VBox listaIngredientes;
    private Label lblResumenAlertas;

    private Label lblTituloFormulario;
    private VBox panelFormulario;
    private ComboBox<Ingrediente> cmbIngrediente;
    private TextField txtCantidad;
    private ComboBox<String> cmbSentido;
    private TextField txtMotivo;
    private TextField txtNombreNuevo;
    private ComboBox<String> cmbUnidad;
    private TextField txtStockInicial;
    private TextField txtStockMinimo;
    private Label lblAviso;
    private Button btnAplicar;

    private Label lblDetalleNombre;
    private Label lblDetalleStock;
    private Label lblDetalleEstado;
    private VBox listaPlatosAfectados;
    private VBox listaMovimientos;

    public InventarioView(Datos datos, String css) {
        this.datos = datos;
        this.css = css;
        numero.setMaximumFractionDigits(2);

        setLeft(crearPanelLista());
        setCenter(crearPanelFormulario());
        setRight(crearPanelDetalle());

        if (!datos.getInventario().isEmpty()) {
            seleccionado = datos.getInventario().get(0);
        }
        reconstruirLista();
        mostrarFormulario();
        refrescarDetalle();
    }

    /** Se llama al entrar al módulo: refleja compras, ajustes y el consumo de órdenes. */
    public void alActivar() {
        reconstruirLista();
        cmbIngrediente.setItems(FXCollections.observableArrayList(datos.getInventario()));
        if (seleccionado != null) {
            cmbIngrediente.setValue(seleccionado);
        }
        refrescarDetalle();
    }

    // ------------------------------------------------------------------
    // Construcción de la interfaz
    // ------------------------------------------------------------------

    private VBox crearPanelLista() {
        Label titulo = crearTitulo("INGREDIENTES");
        lblResumenAlertas = new Label();
        lblResumenAlertas.setWrapText(true);

        listaIngredientes = new VBox(10);
        ScrollPane scroll = new ScrollPane(listaIngredientes);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("scroll-menu");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        grupoIngredientes.selectedToggleProperty().addListener((obs, anterior, nuevo) -> {
            if (actualizandoLista) {
                return;
            }
            if (nuevo == null) {
                if (anterior != null) {
                    anterior.setSelected(true);
                }
                return;
            }
            Ingrediente elegido = (Ingrediente) nuevo.getUserData();
            if (elegido != seleccionado) {
                seleccionado = elegido;
                cmbIngrediente.setValue(elegido);
                refrescarDetalle();
            }
        });

        VBox leyenda = new VBox(6,
                crearLeyenda("leyenda-libre", "Stock suficiente"),
                crearLeyenda("leyenda-ocupada", "Stock mínimo / agotado"));
        leyenda.setPadding(new Insets(10, 0, 0, 0));

        VBox panel = new VBox(14, titulo, lblResumenAlertas, scroll, leyenda);
        panel.setPadding(new Insets(20));
        panel.setPrefWidth(280);
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

    private ScrollPane crearPanelFormulario() {
        lblTituloFormulario = crearTitulo("REGISTRAR COMPRA");

        ToggleGroup grupoAccion = new ToggleGroup();
        ToggleButton btnCompra = crearAccion("Registrar compra", grupoAccion, Accion.COMPRA);
        ToggleButton btnAjuste = crearAccion("Ajustar existencias", grupoAccion, Accion.AJUSTE);
        ToggleButton btnNuevo = crearAccion("Ingrediente nuevo", grupoAccion, Accion.NUEVO);
        btnCompra.setSelected(true);
        HBox acciones = new HBox(10, btnCompra, btnAjuste, btnNuevo);

        cmbIngrediente = new ComboBox<>(FXCollections.observableArrayList(datos.getInventario()));
        cmbIngrediente.setPromptText("Selecciona un ingrediente");
        cmbIngrediente.setMaxWidth(Double.MAX_VALUE);
        cmbIngrediente.getStyleClass().add("campo");
        cmbIngrediente.valueProperty().addListener((obs, a, n) -> {
            if (n != null && n != seleccionado) {
                seleccionado = n;
                reconstruirLista();
                refrescarDetalle();
            }
        });

        txtCantidad = new TextField();
        txtCantidad.setPromptText("Ej: 500");
        txtCantidad.getStyleClass().add("campo");

        cmbSentido = new ComboBox<>(FXCollections.observableArrayList("Incremento (entrada)", "Decremento (salida)"));
        cmbSentido.setValue("Incremento (entrada)");
        cmbSentido.setMaxWidth(Double.MAX_VALUE);
        cmbSentido.getStyleClass().add("campo");

        txtMotivo = new TextField();
        txtMotivo.setPromptText("Ej: merma, corrección de conteo");
        txtMotivo.getStyleClass().add("campo");

        txtNombreNuevo = new TextField();
        txtNombreNuevo.setPromptText("Ej: Cebolla cabezona");
        txtNombreNuevo.getStyleClass().add("campo");

        cmbUnidad = new ComboBox<>(FXCollections.observableArrayList("g", "ml", "unidad"));
        cmbUnidad.setValue("g");
        cmbUnidad.setMaxWidth(Double.MAX_VALUE);
        cmbUnidad.getStyleClass().add("campo");

        txtStockInicial = new TextField();
        txtStockInicial.setPromptText("Ej: 1000");
        txtStockInicial.getStyleClass().add("campo");

        txtStockMinimo = new TextField();
        txtStockMinimo.setPromptText("Ej: 200");
        txtStockMinimo.getStyleClass().add("campo");

        panelFormulario = new VBox(16);

        lblAviso = new Label();
        lblAviso.getStyleClass().add("aviso");
        lblAviso.setWrapText(true);
        lblAviso.setVisible(false);
        lblAviso.setManaged(false);

        btnAplicar = new Button("Registrar compra");
        btnAplicar.getStyleClass().add("btn-rojo");
        btnAplicar.setMaxWidth(Double.MAX_VALUE);
        btnAplicar.setOnAction(e -> aplicar());
        mostrarFormulario();

        Label ayuda = new Label("Las cantidades deben ser mayores a cero. Un ajuste no puede dejar la existencia en negativo.");
        ayuda.getStyleClass().add("orden-abierta");
        ayuda.setWrapText(true);

        VBox contenido = new VBox(16, lblTituloFormulario, acciones, panelFormulario, ayuda, lblAviso, btnAplicar);
        contenido.setPadding(new Insets(24));

        ScrollPane scroll = new ScrollPane(contenido);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("scroll-menu");
        return scroll;
    }

    private ToggleButton crearAccion(String texto, ToggleGroup grupo, Accion valor) {
        ToggleButton b = new ToggleButton(texto);
        b.setToggleGroup(grupo);
        b.getStyleClass().add("plato-item");
        b.setOnAction(e -> {
            accion = valor;
            mostrarFormulario();
            limpiarAviso();
        });
        return b;
    }

    private VBox crearPanelDetalle() {
        Label titulo = crearTitulo("ESTADO Y MOVIMIENTOS");

        lblDetalleNombre = new Label();
        lblDetalleNombre.getStyleClass().add("orden-mesa");
        lblDetalleNombre.setWrapText(true);

        lblDetalleStock = new Label();
        lblDetalleStock.getStyleClass().add("tarjeta-precio");
        lblDetalleStock.setWrapText(true);

        lblDetalleEstado = new Label();
        lblDetalleEstado.setWrapText(true);

        Label titPlatos = new Label("PLATOS QUE LO USAN");
        titPlatos.getStyleClass().add("cuenta-titulo");
        listaPlatosAfectados = new VBox(6);

        Label titMov = new Label("ÚLTIMOS MOVIMIENTOS");
        titMov.getStyleClass().add("cuenta-titulo");
        listaMovimientos = new VBox(8);
        ScrollPane scrollMov = new ScrollPane(listaMovimientos);
        scrollMov.setFitToWidth(true);
        scrollMov.getStyleClass().add("scroll-orden");
        VBox.setVgrow(scrollMov, Priority.ALWAYS);

        VBox panel = new VBox(12, titulo, lblDetalleNombre, lblDetalleStock, lblDetalleEstado,
                titPlatos, listaPlatosAfectados, titMov, scrollMov);
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

    private VBox crearCampo(String etiqueta, Node campo) {
        Label l = new Label(etiqueta);
        l.getStyleClass().add("etiqueta-campo");
        return new VBox(4, l, campo);
    }

    // ------------------------------------------------------------------
    // Lógica
    // ------------------------------------------------------------------

    private void mostrarFormulario() {
        if (panelFormulario == null) {
            return;
        }
        panelFormulario.getChildren().clear();
        switch (accion) {
            case COMPRA -> {
                lblTituloFormulario.setText("REGISTRAR COMPRA");
                btnAplicar.setText("Registrar compra");
                panelFormulario.getChildren().addAll(
                        crearCampo("Ingrediente", cmbIngrediente),
                        crearCampo("Cantidad recibida", txtCantidad));
            }
            case AJUSTE -> {
                lblTituloFormulario.setText("AJUSTAR EXISTENCIAS");
                btnAplicar.setText("Guardar ajuste");
                panelFormulario.getChildren().addAll(
                        crearCampo("Ingrediente", cmbIngrediente),
                        crearCampo("Sentido del ajuste", cmbSentido),
                        crearCampo("Cantidad", txtCantidad),
                        crearCampo("Motivo (obligatorio)", txtMotivo));
            }
            case NUEVO -> {
                lblTituloFormulario.setText("INGREDIENTE NUEVO");
                btnAplicar.setText("Crear ingrediente");
                panelFormulario.getChildren().addAll(
                        crearCampo("Nombre", txtNombreNuevo),
                        crearCampo("Unidad de medida", cmbUnidad),
                        crearCampo("Existencia inicial", txtStockInicial),
                        crearCampo("Stock mínimo (RF-03)", txtStockMinimo));
            }
        }
    }

    private void aplicar() {
        String error;
        if (accion == Accion.NUEVO) {
            Double inicial = leerNumero(txtStockInicial.getText());
            Double minimo = leerNumero(txtStockMinimo.getText());
            if (inicial == null) {
                mostrarAviso("Ingresa una existencia inicial válida (cero o más).");
                return;
            }
            if (minimo == null) {
                mostrarAviso("Ingresa un stock mínimo válido (cero o más).");
                return;
            }
            error = datos.crearIngrediente(txtNombreNuevo.getText(), cmbUnidad.getValue(),
                    inicial, minimo, Datos.USUARIO_ADMIN);
        } else {
            Double cantidad = leerNumero(txtCantidad.getText());
            if (cantidad == null || !(cantidad > 0)) {
                mostrarAviso("Ingresa una cantidad válida, mayor a cero.");
                return;
            }
            Ingrediente ing = cmbIngrediente.getValue();
            if (accion == Accion.COMPRA) {
                error = datos.registrarCompra(ing, cantidad, Datos.USUARIO_ADMIN);
            } else {
                boolean salida = cmbSentido.getValue() != null && cmbSentido.getValue().startsWith("Decremento");
                double delta = salida ? -cantidad : cantidad;
                error = datos.ajustarExistencias(ing, delta, txtMotivo.getText(), Datos.USUARIO_ADMIN);
            }
            if (error == null) {
                seleccionado = ing;
            }
        }

        if (error != null) {
            mostrarAviso(error);
            return;
        }

        String mensaje;
        if (accion == Accion.NUEVO) {
            seleccionado = datos.getInventario().get(datos.getInventario().size() - 1);
            mensaje = "Se creó \"" + seleccionado.getNombre() + "\" con "
                    + numero.format(seleccionado.getStock()) + " " + seleccionado.getUnidad() + ".";
            txtNombreNuevo.clear();
            txtStockInicial.clear();
            txtStockMinimo.clear();
        } else if (accion == Accion.COMPRA) {
            mensaje = "Compra registrada. Nueva existencia de " + seleccionado.getNombre() + ": "
                    + numero.format(seleccionado.getStock()) + " " + seleccionado.getUnidad() + ".";
            txtCantidad.clear();
        } else {
            mensaje = "Ajuste registrado. Nueva existencia de " + seleccionado.getNombre() + ": "
                    + numero.format(seleccionado.getStock()) + " " + seleccionado.getUnidad() + ".";
            txtCantidad.clear();
            txtMotivo.clear();
        }
        mensaje += "\n\n" + textoImpactoPlatos(seleccionado);

        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle("Inventario actualizado");
        alerta.setHeaderText(headerAlerta(seleccionado));
        alerta.setContentText(mensaje);
        alerta.getDialogPane().getStylesheets().add(css);
        alerta.showAndWait();

        cmbIngrediente.setItems(FXCollections.observableArrayList(datos.getInventario()));
        cmbIngrediente.setValue(seleccionado);
        limpiarAviso();
        reconstruirLista();
        refrescarDetalle();
    }

    private String headerAlerta(Ingrediente ing) {
        if (ing.estaAgotado()) {
            return "Ingrediente agotado: " + ing.getNombre();
        }
        if (ing.estaEnAlerta()) {
            return "Alerta de stock mínimo: " + ing.getNombre();
        }
        return "Movimiento guardado";
    }

    private String textoImpactoPlatos(Ingrediente ing) {
        List<Item> afectados = datos.platosQueUsan(ing);
        if (afectados.isEmpty()) {
            return "Ningún plato del menú usa este ingrediente todavía.";
        }
        StringBuilder sb = new StringBuilder("Disponibilidad recalculada:\n");
        for (Item plato : afectados) {
            int porciones = plato.getPorciones();
            sb.append("- ").append(plato.getNombre()).append(": ");
            if (porciones <= 0) {
                sb.append("deshabilitado (agotado)");
            } else {
                sb.append(porciones).append(" porciones");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    private Double leerNumero(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            return null;
        }
        try {
            return Double.parseDouble(texto.trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // ------------------------------------------------------------------
    // Actualización de la interfaz
    // ------------------------------------------------------------------

    private void reconstruirLista() {
        actualizandoLista = true;
        grupoIngredientes.getToggles().clear();
        listaIngredientes.getChildren().clear();

        int alertas = datos.ingredientesEnAlerta().size();
        lblResumenAlertas.getStyleClass().removeAll("aviso", "disp-ok");
        if (alertas == 0) {
            lblResumenAlertas.setText("Todos los insumos están por encima del stock mínimo.");
            lblResumenAlertas.getStyleClass().add("disp-ok");
        } else {
            lblResumenAlertas.setText(alertas + " ingrediente(s) en alerta de stock mínimo (RF-03).");
            lblResumenAlertas.getStyleClass().add("aviso");
        }

        for (Ingrediente ing : datos.getInventario()) {
            ToggleButton b = new ToggleButton(ing.getNombre() + "\n"
                    + numero.format(ing.getStock()) + " " + ing.getUnidad()
                    + "  ·  mín. " + numero.format(ing.getStockMinimo()));
            b.setUserData(ing);
            b.setToggleGroup(grupoIngredientes);
            b.getStyleClass().add("plato-item");
            if (ing.estaAgotado()) {
                b.getStyleClass().add("ing-item-agotado");
            } else if (ing.estaEnAlerta()) {
                b.getStyleClass().add("ing-item-alerta");
            }
            b.setMaxWidth(Double.MAX_VALUE);
            listaIngredientes.getChildren().add(b);
            if (ing == seleccionado) {
                b.setSelected(true);
            }
        }
        actualizandoLista = false;
    }

    private void refrescarDetalle() {
        if (seleccionado == null) {
            lblDetalleNombre.setText("Sin ingrediente");
            lblDetalleStock.setText("");
            lblDetalleEstado.setText("");
            listaPlatosAfectados.getChildren().clear();
            listaMovimientos.getChildren().clear();
            return;
        }
        Ingrediente ing = seleccionado;
        lblDetalleNombre.setText(ing.getNombre());
        lblDetalleStock.setText(numero.format(ing.getStock()) + " " + ing.getUnidad()
                + "  (mín. " + numero.format(ing.getStockMinimo()) + ")");

        lblDetalleEstado.getStyleClass().removeAll("disp-ok", "disp-agotado");
        if (ing.estaAgotado()) {
            lblDetalleEstado.setText("Agotado: los platos que lo requieren quedan deshabilitados.");
            lblDetalleEstado.getStyleClass().add("disp-agotado");
        } else if (ing.estaEnAlerta()) {
            lblDetalleEstado.setText("Alerta la existencia es menor o igual al stock mínimo.");
            lblDetalleEstado.getStyleClass().add("disp-agotado");
        } else {
            lblDetalleEstado.setText("Existencia suficiente.");
            lblDetalleEstado.getStyleClass().add("disp-ok");
        }

        listaPlatosAfectados.getChildren().clear();
        List<Item> afectados = datos.platosQueUsan(ing);
        if (afectados.isEmpty()) {
            Label vacio = new Label("Ningún plato usa este insumo.");
            vacio.getStyleClass().add("vacio");
            listaPlatosAfectados.getChildren().add(vacio);
        }
        for (Item plato : afectados) {
            int porciones = plato.getPorciones();
            Label fila = new Label(plato.getNombre() + "  ·  "
                    + (porciones <= 0 ? "No disponible" : porciones + " porciones"));
            fila.setWrapText(true);
            fila.getStyleClass().add(porciones <= 0 ? "ing-faltante" : "cuenta-fila");
            listaPlatosAfectados.getChildren().add(fila);
        }

        listaMovimientos.getChildren().clear();
        int mostrados = 0;
        for (MovimientoInventario mov : datos.getMovimientos()) {
            if (mov.getIngrediente() != ing) {
                continue;
            }
            listaMovimientos.getChildren().add(crearFilaMovimiento(mov));
            mostrados++;
            if (mostrados >= 12) {
                break;
            }
        }
        if (mostrados == 0) {
            Label vacio = new Label("Aún no hay movimientos de este ingrediente.");
            vacio.getStyleClass().add("vacio");
            listaMovimientos.getChildren().add(vacio);
        }
    }

    private VBox crearFilaMovimiento(MovimientoInventario mov) {
        Label tipo = new Label(etiquetaTipo(mov.getTipo()) + "  ·  " + fechaHora.format(mov.getFechaHora()));
        tipo.getStyleClass().add("fila-nombre");
        tipo.setWrapText(true);

        String signo = mov.getCantidad() >= 0 ? "+" : "";
        Label detalle = new Label(signo + numero.format(mov.getCantidad()) + " " + mov.getIngrediente().getUnidad()
                + "  →  " + numero.format(mov.getExistenciaNueva()) + " " + mov.getIngrediente().getUnidad()
                + "\n" + mov.getUsuario() + ": " + mov.getMotivo());
        detalle.getStyleClass().add("cuenta-fila");
        detalle.setWrapText(true);

        VBox caja = new VBox(2, tipo, detalle);
        caja.getStyleClass().add("fila-orden");
        return caja;
    }

    private String etiquetaTipo(MovimientoInventario.Tipo tipo) {
        return switch (tipo) {
            case COMPRA -> "Compra";
            case AJUSTE -> "Ajuste";
            case CONSUMO -> "Consumo";
            case ALTA -> "Alta";
        };
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
