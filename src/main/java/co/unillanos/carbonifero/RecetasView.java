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
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Módulo Gestión de recetas de los platos.
 * Permite crear platos y definir sus ingredientes con la cantidad por porción,
 * mostrando la disponibilidad según el inventario compartido (Datos).
 */
public class RecetasView extends BorderPane {

    private final Datos datos;
    private final String css;
    private final NumberFormat moneda = NumberFormat.getIntegerInstance(Locale.forLanguageTag("es-CO"));
    private final NumberFormat numero = NumberFormat.getNumberInstance(Locale.forLanguageTag("es-CO"));

    // Estado de edición
    private final List<LineaReceta> lineas = new ArrayList<>(); // receta que se está editando
    private Item platoActual = null;                             // null = plato nuevo
    private boolean actualizandoLista = false;

    // Componentes que se actualizan
    private final ToggleGroup grupoPlatos = new ToggleGroup();
    private VBox listaPlatos;
    private Label lblTituloEditor;
    private TextField txtNombre;
    private TextField txtPrecio;
    private TextArea txtDescripcion;
    private ComboBox<Ingrediente> cmbIngrediente;
    private TextField txtCantidad;
    private Label lblUnidad;
    private VBox listaIngredientes;
    private Label lblResNombre;
    private Label lblResPrecio;
    private VBox listaResumen;
    private Label lblPorciones;
    private Label lblEstado;
    private Label lblAviso;
    private Button btnGuardar;
    private Button btnCancelar;

    public RecetasView(Datos datos, String css) {
        this.datos = datos;
        this.css = css;
        numero.setMaximumFractionDigits(2);

        setLeft(crearPanelPlatos());
        setCenter(crearPanelEditor());
        setRight(crearPanelResumen());

        // El resumen se actualiza mientras se escribe
        txtNombre.textProperty().addListener((obs, a, n) -> refrescarResumen());
        txtPrecio.textProperty().addListener((obs, a, n) -> refrescarResumen());

        platoActual = datos.getPlatos().get(0);
        reconstruirListaPlatos();
        cargarPlato(platoActual);
    }

    /** Se llama al entrar al módulo: toma los cambios del inventario sin perder lo que se está editando. */
    public void alActivar() {
        reconstruirListaPlatos();
        refrescar();
    }

    // ------------------------------------------------------------------
    // Construcción de la interfaz
    // ------------------------------------------------------------------

    // Panel izquierdo: lista de platos
    private VBox crearPanelPlatos() {
        Label titulo = crearTitulo("PLATOS");

        Button nuevo = new Button("+ Nuevo plato");
        nuevo.getStyleClass().add("btn-rojo");
        nuevo.setMaxWidth(Double.MAX_VALUE);
        nuevo.setOnAction(e -> nuevoPlato());

        listaPlatos = new VBox(10);
        ScrollPane scroll = new ScrollPane(listaPlatos);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("scroll-menu");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        // Evita que el plato quede sin selección al volver a hacer clic
        grupoPlatos.selectedToggleProperty().addListener((obs, anterior, nuevoToggle) -> {
            if (actualizandoLista) {
                return;
            }
            if (nuevoToggle == null) {
                if (anterior != null) {
                    anterior.setSelected(true);
                }
                return;
            }
            Item elegido = (Item) nuevoToggle.getUserData();
            if (elegido != platoActual) {
                cargarPlato(elegido);
            }
        });

        VBox leyenda = new VBox(6,
                crearLeyenda("leyenda-libre", "Disponible"),
                crearLeyenda("leyenda-ocupada", "Sin inventario suficiente"));
        leyenda.setPadding(new Insets(10, 0, 0, 0));

        VBox panel = new VBox(14, titulo, nuevo, scroll, leyenda);
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

    // Panel central: formulario del plato y de sus ingredientes
    private ScrollPane crearPanelEditor() {
        lblTituloEditor = crearTitulo("NUEVO PLATO");

        txtNombre = new TextField();
        txtNombre.setPromptText("Ej: Lomo al carbón");
        txtNombre.getStyleClass().add("campo");

        txtPrecio = new TextField();
        txtPrecio.setPromptText("Ej: 38000");
        txtPrecio.getStyleClass().add("campo");

        txtDescripcion = new TextArea();
        txtDescripcion.setPromptText("Descripción breve del plato");
        txtDescripcion.setPrefRowCount(3);
        txtDescripcion.setWrapText(true);
        txtDescripcion.getStyleClass().add("campo");

        VBox campoNombre = crearCampo("Nombre del plato", txtNombre);
        HBox.setHgrow(campoNombre, Priority.ALWAYS);
        VBox campoPrecio = crearCampo("Precio ($)", txtPrecio);
        campoPrecio.setPrefWidth(170);
        HBox filaDatos = new HBox(16, campoNombre, campoPrecio);

        // Agregar ingrediente a la receta
        cmbIngrediente = new ComboBox<>(FXCollections.observableArrayList(datos.getInventario()));
        cmbIngrediente.setPromptText("Selecciona un ingrediente");
        cmbIngrediente.setPrefWidth(280);
        cmbIngrediente.getStyleClass().add("campo");
        cmbIngrediente.valueProperty().addListener((obs, a, n) ->
                lblUnidad.setText(n == null ? "" : n.getUnidad()));

        txtCantidad = new TextField();
        txtCantidad.setPromptText("Ej: 250");
        txtCantidad.setPrefWidth(120);
        txtCantidad.getStyleClass().add("campo");

        lblUnidad = new Label();
        lblUnidad.getStyleClass().add("fila-nombre");
        lblUnidad.setMinWidth(60);
        lblUnidad.setPadding(new Insets(0, 0, 8, 0));

        Button agregar = new Button("Agregar");
        agregar.getStyleClass().add("btn-rojo");
        agregar.setOnAction(e -> agregarIngrediente());

        HBox filaAgregar = new HBox(12,
                crearCampo("Ingrediente (del inventario)", cmbIngrediente),
                crearCampo("Cantidad por porción", txtCantidad),
                lblUnidad, agregar);
        filaAgregar.setAlignment(Pos.BOTTOM_LEFT);

        Label ayuda = new Label("Indica cuánto de cada ingrediente lleva una porción del plato.");
        ayuda.getStyleClass().add("orden-abierta");

        listaIngredientes = new VBox(8);

        VBox contenido = new VBox(16,
                lblTituloEditor, filaDatos, crearCampo("Descripción", txtDescripcion),
                crearTitulo("INGREDIENTES DE LA RECETA"), ayuda, filaAgregar,
                crearCabeceraTabla(), listaIngredientes);
        contenido.setPadding(new Insets(24));

        ScrollPane scroll = new ScrollPane(contenido);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("scroll-menu");
        return scroll;
    }

    private VBox crearCampo(String etiqueta, Node campo) {
        Label l = new Label(etiqueta);
        l.getStyleClass().add("etiqueta-campo");
        return new VBox(4, l, campo);
    }

    private HBox crearCabeceraTabla() {
        Label ingrediente = crearCabecera("INGREDIENTE", 190);
        Label cantidad = crearCabecera("POR PORCIÓN", 110);
        Label disponible = crearCabecera("INVENTARIO", 190);
        HBox cabecera = new HBox(10, ingrediente, cantidad, disponible);
        cabecera.getStyleClass().add("cabecera-tabla");
        return cabecera;
    }

    private Label crearCabecera(String texto, double ancho) {
        Label l = new Label(texto);
        l.getStyleClass().add("cabecera-texto");
        l.setPrefWidth(ancho);
        return l;
    }

    // Panel derecho: resumen de la receta y acciones
    private VBox crearPanelResumen() {
        Label titulo = crearTitulo("RESUMEN DE LA RECETA");

        lblResNombre = new Label();
        lblResNombre.getStyleClass().add("orden-mesa");
        lblResNombre.setWrapText(true);

        lblResPrecio = new Label();
        lblResPrecio.getStyleClass().add("tarjeta-precio");

        listaResumen = new VBox(6);
        ScrollPane scroll = new ScrollPane(listaResumen);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("scroll-orden");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        Label textoPorciones = new Label("PORCIONES POSIBLES");
        textoPorciones.getStyleClass().add("total-texto");
        lblPorciones = new Label("0");
        lblPorciones.getStyleClass().add("total-valor");
        Region espacio = new Region();
        HBox.setHgrow(espacio, Priority.ALWAYS);
        HBox filaPorciones = new HBox(textoPorciones, espacio, lblPorciones);
        filaPorciones.setAlignment(Pos.CENTER_LEFT);
        filaPorciones.getStyleClass().add("fila-total");

        lblEstado = new Label();
        lblEstado.setWrapText(true);

        Label nota = new Label("La disponibilidad se calcula con el inventario de ejemplo.");
        nota.getStyleClass().add("orden-abierta");
        nota.setWrapText(true);

        lblAviso = new Label();
        lblAviso.getStyleClass().add("aviso");
        lblAviso.setWrapText(true);
        lblAviso.setVisible(false);
        lblAviso.setManaged(false);

        btnGuardar = new Button("Guardar receta");
        btnGuardar.getStyleClass().add("btn-rojo");
        btnGuardar.setMaxWidth(Double.MAX_VALUE);
        btnGuardar.setOnAction(e -> guardar());

        btnCancelar = new Button("Descartar cambios");
        btnCancelar.getStyleClass().add("btn-borde");
        btnCancelar.setMaxWidth(Double.MAX_VALUE);
        btnCancelar.setOnAction(e -> descartar());

        VBox panel = new VBox(12, titulo, lblResNombre, lblResPrecio, scroll, filaPorciones,
                lblEstado, nota, lblAviso, btnGuardar, btnCancelar);
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

    private HBox crearFilaIngrediente(LineaReceta linea) {
        Ingrediente ing = linea.getIngrediente();

        Label nombre = new Label(ing.getNombre());
        nombre.getStyleClass().add("fila-nombre");
        nombre.setWrapText(true);
        nombre.setPrefWidth(190);

        Label cantidad = new Label(numero.format(linea.getCantidad()) + " " + ing.getUnidad());
        cantidad.getStyleClass().add("ing-dato");
        cantidad.setPrefWidth(110);

        Label inventarioLbl = new Label();
        inventarioLbl.setPrefWidth(190);
        if (linea.alcanza()) {
            inventarioLbl.setText("Hay " + numero.format(ing.getStock()) + " " + ing.getUnidad());
            inventarioLbl.getStyleClass().add("ing-ok");
        } else {
            inventarioLbl.setText("Insuficiente (" + numero.format(ing.getStock()) + " " + ing.getUnidad() + ")");
            inventarioLbl.getStyleClass().add("ing-faltante");
        }

        Button quitar = new Button("×");
        quitar.getStyleClass().add("btn-mini");
        quitar.setOnAction(e -> quitarIngrediente(linea));

        Region espacio = new Region();
        HBox.setHgrow(espacio, Priority.ALWAYS);

        HBox fila = new HBox(10, nombre, cantidad, inventarioLbl, espacio, quitar);
        fila.setAlignment(Pos.CENTER_LEFT);
        fila.getStyleClass().add("fila-orden");
        return fila;
    }

    // ------------------------------------------------------------------
    // Lógica del prototipo
    // ------------------------------------------------------------------
    private void nuevoPlato() {
        platoActual = null;
        actualizandoLista = true;
        grupoPlatos.selectToggle(null);
        actualizandoLista = false;

        txtNombre.clear();
        txtPrecio.clear();
        txtDescripcion.clear();
        txtCantidad.clear();
        cmbIngrediente.setValue(null);
        lineas.clear();
        limpiarAviso();
        refrescar();
    }

    private void cargarPlato(Item plato) {
        platoActual = plato;
        txtNombre.setText(plato.getNombre());
        txtPrecio.setText(String.valueOf(plato.getPrecio()));
        txtDescripcion.setText(plato.getDescripcion());
        txtCantidad.clear();
        cmbIngrediente.setValue(null);
        lineas.clear();
        for (LineaReceta l : plato.getReceta()) {
            lineas.add(new LineaReceta(l.getIngrediente(), l.getCantidad())); // copia de trabajo
        }
        limpiarAviso();
        refrescar();
    }

    private void agregarIngrediente() {
        Ingrediente ing = cmbIngrediente.getValue();
        if (ing == null) {
            mostrarAviso("Selecciona un ingrediente del inventario.");
            return;
        }
        double cantidad;
        try {
            cantidad = Double.parseDouble(txtCantidad.getText().trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            cantidad = -1;
        }
        if (!(cantidad > 0) || Double.isInfinite(cantidad)) {
            mostrarAviso("Ingresa una cantidad válida, mayor a cero.");
            return;
        }

        LineaReceta existente = null;
        for (LineaReceta l : lineas) {
            if (l.getIngrediente() == ing) {
                existente = l;
            }
        }
        if (existente != null) {
            existente.setCantidad(cantidad); // si ya estaba, se actualiza la medida
        } else {
            lineas.add(new LineaReceta(ing, cantidad));
        }
        txtCantidad.clear();
        cmbIngrediente.setValue(null);
        limpiarAviso();
        refrescar();
    }

    private void quitarIngrediente(LineaReceta linea) {
        lineas.remove(linea);
        limpiarAviso();
        refrescar();
    }

    private void descartar() {
        if (platoActual != null) {
            cargarPlato(platoActual);
        } else {
            nuevoPlato();
        }
    }

    private void guardar() {
        String nombre = txtNombre.getText().trim();
        Integer precio = leerPrecio();

        if (nombre.isEmpty()) {
            mostrarAviso("Escribe el nombre del plato.");
            return;
        }
        if (precio == null || precio <= 0) {
            mostrarAviso("Ingresa un precio válido (solo números).");
            return;
        }
        if (lineas.isEmpty()) {
            mostrarAviso("Agrega al menos un ingrediente a la receta.");
            return;
        }
        for (Item p : datos.getPlatos()) {
            if (p != platoActual && p.getNombre().equalsIgnoreCase(nombre)) {
                mostrarAviso("Ya existe un plato llamado \"" + p.getNombre() + "\".");
                return;
            }
        }

        boolean esNuevo = platoActual == null;
        Item plato = esNuevo ? new Item(nombre, "", precio, Item.Tipo.PLATO) : platoActual;
        plato.setNombre(nombre);
        plato.setDescripcion(txtDescripcion.getText().trim());
        plato.setPrecio(precio);
        plato.getReceta().clear();
        for (LineaReceta l : lineas) {
            plato.getReceta().add(new LineaReceta(l.getIngrediente(), l.getCantidad()));
        }
        if (esNuevo) {
            datos.getPlatos().add(plato);
        }
        platoActual = plato;
        reconstruirListaPlatos();

        StringBuilder detalle = new StringBuilder();
        for (LineaReceta l : plato.getReceta()) {
            detalle.append("- ").append(numero.format(l.getCantidad())).append(" ")
                    .append(l.getIngrediente().getUnidad()).append(" de ")
                    .append(l.getIngrediente().getNombre()).append("\n");
        }
        String mensaje = "Ingredientes por porción:\n" + detalle
                + "\nPorciones que se pueden preparar con el inventario actual: " + plato.getPorciones()
                + "\n\nYa aparece en el módulo Ordenar platos.";

        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle("Receta guardada");
        alerta.setHeaderText((esNuevo ? "Plato creado: " : "Receta actualizada: ") + plato.getNombre());
        alerta.setContentText(mensaje);
        alerta.getDialogPane().getStylesheets().add(css);
        alerta.showAndWait();

        limpiarAviso();
        refrescar();
    }

    private Integer leerPrecio() {
        String digitos = txtPrecio.getText().replaceAll("[^0-9]", "");
        if (digitos.isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(digitos);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // ------------------------------------------------------------------
    // Actualización de la interfaz
    // ------------------------------------------------------------------
    private void reconstruirListaPlatos() {
        actualizandoLista = true;
        grupoPlatos.getToggles().clear();
        listaPlatos.getChildren().clear();
        for (Item p : datos.getPlatos()) {
            int porciones = p.getPorciones();
            ToggleButton b = new ToggleButton(p.getNombre() + "\n" + textoDisponibilidad(porciones));
            b.setUserData(p);
            b.setToggleGroup(grupoPlatos);
            b.getStyleClass().add("plato-item");
            if (porciones <= 0) {
                b.getStyleClass().add("plato-item-agotado");
            }
            b.setMaxWidth(Double.MAX_VALUE);
            listaPlatos.getChildren().add(b);
            if (p == platoActual) {
                b.setSelected(true);
            }
        }
        actualizandoLista = false;
    }

    private String textoDisponibilidad(int porciones) {
        if (porciones <= 0) {
            return "No disponible";
        }
        if (porciones <= Datos.STOCK_MINIMO) {
            return "Stock bajo: quedan " + porciones;
        }
        return "Disponibles: " + porciones;
    }

    private void refrescar() {
        lblTituloEditor.setText(platoActual == null ? "NUEVO PLATO" : "EDITAR PLATO");

        listaIngredientes.getChildren().clear();
        if (lineas.isEmpty()) {
            Label vacio = new Label("Aún no hay ingredientes en la receta.");
            vacio.getStyleClass().add("vacio");
            listaIngredientes.getChildren().add(vacio);
        }
        for (LineaReceta l : lineas) {
            listaIngredientes.getChildren().add(crearFilaIngrediente(l));
        }
        refrescarResumen();
    }

    private void refrescarResumen() {
        if (lblResNombre == null) {
            return;
        }
        String nombre = txtNombre.getText().trim();
        lblResNombre.setText(nombre.isEmpty() ? "(sin nombre)" : nombre);
        Integer precio = leerPrecio();
        lblResPrecio.setText("$ " + moneda.format(precio == null ? 0 : precio));

        listaResumen.getChildren().clear();
        List<String> faltantes = new ArrayList<>();
        for (LineaReceta l : lineas) {
            Label detalle = new Label(numero.format(l.getCantidad()) + " " + l.getIngrediente().getUnidad()
                    + "  " + l.getIngrediente().getNombre());
            detalle.getStyleClass().add("cuenta-fila");
            detalle.setWrapText(true);
            Region espacio = new Region();
            HBox.setHgrow(espacio, Priority.ALWAYS);
            HBox fila = new HBox(8, detalle, espacio);
            fila.setAlignment(Pos.CENTER_LEFT);
            if (!l.alcanza()) {
                Label falta = new Label("Falta");
                falta.getStyleClass().add("aviso");
                fila.getChildren().add(falta);
                faltantes.add(l.getIngrediente().getNombre());
            }
            listaResumen.getChildren().add(fila);
        }
        if (lineas.isEmpty()) {
            Label vacio = new Label("Sin ingredientes todavía.");
            vacio.getStyleClass().add("vacio");
            listaResumen.getChildren().add(vacio);
        }

        int porciones = Item.calcularPorciones(lineas);
        lblPorciones.setText(String.valueOf(porciones));

        lblEstado.getStyleClass().removeAll("disp-ok", "disp-agotado");
        if (lineas.isEmpty()) {
            lblEstado.setText("Agrega ingredientes para calcular la disponibilidad.");
            lblEstado.getStyleClass().add("disp-ok");
        } else if (porciones <= 0) {
            lblEstado.setText("No disponible: inventario insuficiente de " + String.join(", ", faltantes) + ".");
            lblEstado.getStyleClass().add("disp-agotado");
        } else if (porciones <= Datos.STOCK_MINIMO) {
            lblEstado.setText("Disponible con stock bajo (" + porciones + " porciones).");
            lblEstado.getStyleClass().add("disp-agotado");
        } else {
            lblEstado.setText("Disponible para ordenar.");
            lblEstado.getStyleClass().add("disp-ok");
        }
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
