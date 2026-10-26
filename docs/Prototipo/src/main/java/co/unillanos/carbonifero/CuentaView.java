package co.unillanos.carbonifero;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Separator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

/**
 * Consulta las cuentas pendientes y permite iniciar su pago.
 */
public class CuentaView extends BorderPane {

    private final OrdenesView ordenes;
    private final String css;
    private final NumberFormat moneda = NumberFormat.getIntegerInstance(Locale.forLanguageTag("es-CO"));

    private final ListView<Integer> listaMesas = new ListView<>();
    private final VBox detalle = new VBox(12);
    private final Label lblTitulo = new Label("Selecciona una cuenta");
    private final Label lblTotal = new Label("$ 0");
    private final Button btnPagar = new Button("Continuar al pago");

    private Consumer<Integer> alSolicitarPago = mesa -> {
    };

    public CuentaView(OrdenesView ordenes, String css) {
        this.ordenes = ordenes;
        this.css = css;

        construirInterfaz();
        listaMesas.getSelectionModel().selectedItemProperty()
                .addListener((obs, anterior, mesa) -> mostrarCuenta(mesa));

        btnPagar.setOnAction(e -> {
            Integer mesa = listaMesas.getSelectionModel().getSelectedItem();
            if (mesa != null) {
                alSolicitarPago.accept(mesa);
            }
        });

        refrescar();
    }

    public void setAlSolicitarPago(Consumer<Integer> accion) {
        alSolicitarPago = accion != null ? accion : mesa -> {
        };
    }

    public void alActivar() {
        refrescar();
    }

    public void refrescar() {
        Integer seleccionada = listaMesas.getSelectionModel().getSelectedItem();

        listaMesas.getItems().setAll(ordenes.getMesasConCuentaAbierta());

        if (seleccionada != null && listaMesas.getItems().contains(seleccionada)) {
            listaMesas.getSelectionModel().select(seleccionada);
        } else if (!listaMesas.getItems().isEmpty()) {
            listaMesas.getSelectionModel().selectFirst();
        } else {
            mostrarCuenta(null);
        }
    }

    private void construirInterfaz() {
        Label tituloMesas = new Label("CUENTAS PENDIENTES");
        tituloMesas.getStyleClass().add("titulo-seccion");

        listaMesas.setPrefWidth(240);
        listaMesas.setPlaceholder(new Label("No hay cuentas pendientes."));

        VBox panelIzquierdo = new VBox(12, tituloMesas, listaMesas);
        panelIzquierdo.setPadding(new Insets(20));
        panelIzquierdo.setPrefWidth(280);
        panelIzquierdo.getStyleClass().add("panel-lateral");

        lblTitulo.getStyleClass().add("titulo-seccion");

        lblTotal.getStyleClass().add("total-valor");

        Region espacio = new Region();
        HBox.setHgrow(espacio, Priority.ALWAYS);

        Label textoTotal = new Label("TOTAL A PAGAR");
        textoTotal.getStyleClass().add("total-texto");

        HBox filaTotal = new HBox(12, textoTotal, espacio, lblTotal);
        filaTotal.setAlignment(Pos.CENTER_LEFT);
        filaTotal.getStyleClass().add("fila-total");

        btnPagar.getStyleClass().add("btn-rojo");
        btnPagar.setMaxWidth(Double.MAX_VALUE);

        detalle.setPadding(new Insets(24));
        detalle.getChildren().addAll(
                lblTitulo,
                new Separator(),
                filaTotal,
                btnPagar);

        VBox.setVgrow(detalle, Priority.ALWAYS);

        setLeft(panelIzquierdo);
        setCenter(detalle);
        getStyleClass().add("vista-cuenta");
    }

    private void mostrarCuenta(Integer mesa) {
        detalle.getChildren().clear();

        if (mesa == null) {
            lblTitulo.setText("No hay cuentas pendientes");
            lblTotal.setText("$ 0");
            btnPagar.setDisable(true);
            detalle.getChildren().addAll(lblTitulo, new Separator());
            return;
        }

        List<LineaOrden> lineas = ordenes.getLineasCuenta(mesa);
        Integer numeroOrden = ordenes.getNumeroOrden(mesa);

        lblTitulo.setText("Orden #" + numeroOrden + " — Mesa " + mesa);

        VBox productos = new VBox(10);
        productos.getChildren().add(new Label("PRODUCTOS"));

        for (LineaOrden linea : lineas) {
            Label nombre = new Label(
                    linea.getCantidad() + " × " + linea.getItem().getNombre());
            nombre.setWrapText(true);

            Label subtotal = new Label("$ " + moneda.format(linea.getSubtotal()));

            Region espacio = new Region();
            HBox.setHgrow(espacio, Priority.ALWAYS);

            HBox fila = new HBox(10, nombre, espacio, subtotal);
            fila.setAlignment(Pos.CENTER_LEFT);
            productos.getChildren().add(fila);
        }

        lblTotal.setText("$ " + moneda.format(ordenes.getTotalCuenta(mesa)));
        btnPagar.setDisable(lineas.isEmpty());

        detalle.getChildren().addAll(
                lblTitulo,
                new Separator(),
                productos,
                new Separator());

        Region espacio = new Region();
        VBox.setVgrow(espacio, Priority.ALWAYS);
        detalle.getChildren().addAll(espacio, crearFilaTotal(), btnPagar);
    }

    private HBox crearFilaTotal() {
        Label texto = new Label("TOTAL A PAGAR");
        texto.getStyleClass().add("total-texto");

        Region espacio = new Region();
        HBox.setHgrow(espacio, Priority.ALWAYS);

        HBox fila = new HBox(12, texto, espacio, lblTotal);
        fila.setAlignment(Pos.CENTER_LEFT);
        fila.getStyleClass().add("fila-total");
        return fila;
    }
}
