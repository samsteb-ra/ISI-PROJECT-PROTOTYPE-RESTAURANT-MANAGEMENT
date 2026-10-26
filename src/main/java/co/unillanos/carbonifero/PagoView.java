
package co.unillanos.carbonifero;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * Permite seleccionar el método de pago y cerrar una cuenta.
 */
public class PagoView extends BorderPane {

    private final OrdenesView ordenes;
    private final List<Pago> pagos;
    private final String css;

    private final NumberFormat moneda =
            NumberFormat.getIntegerInstance(Locale.forLanguageTag("es-CO"));

    private final Label lblOrden = new Label();
    private final Label lblMesa = new Label();
    private final Label lblTotal = new Label("$ 0");

    private final ComboBox<Pago.Metodo> cmbMetodo = new ComboBox<>();
    private final Button btnConfirmar = new Button("Confirmar pago");
    private final Button btnVolver = new Button("Volver a cuentas");

    private Integer mesaActual;
    private Runnable alVolver = () -> { };
    private Runnable alPagar = () -> { };

    public PagoView(OrdenesView ordenes, List<Pago> pagos, String css) {
        this.ordenes = ordenes;
        this.pagos = pagos;
        this.css = css;

        construirInterfaz();
    }

    public void setAlVolver(Runnable accion) {
        alVolver = accion != null ? accion : () -> { };
    }

    public void setAlPagar(Runnable accion) {
        alPagar = accion != null ? accion : () -> { };
    }

    /**
     * Prepara la pantalla con los datos de la mesa seleccionada.
     */
    public void prepararPago(int mesa) {
        this.mesaActual = mesa;

        Integer numeroOrden = ordenes.getNumeroOrden(mesa);
        int total = ordenes.getTotalCuenta(mesa);

        lblOrden.setText(
                numeroOrden == null ? "Orden: no disponible" : "Orden #" + numeroOrden
        );
        lblMesa.setText("Mesa " + mesa);
        lblTotal.setText("$ " + moneda.format(total));

        cmbMetodo.getSelectionModel().clearSelection();
        btnConfirmar.setDisable(
                numeroOrden == null || ordenes.getLineasCuenta(mesa).isEmpty()
        );
    }

    private void construirInterfaz() {
        Label titulo = new Label("CERRAR CUENTA");
        titulo.getStyleClass().add("titulo-seccion");

        lblOrden.getStyleClass().add("orden-abierta");
        lblMesa.getStyleClass().add("orden-mesa");
        lblTotal.getStyleClass().add("total-valor");

        Label textoMetodo = new Label("Método de pago");
        textoMetodo.getStyleClass().add("total-texto");

        cmbMetodo.getItems().setAll(Pago.Metodo.values());
        cmbMetodo.setPromptText("Selecciona un método");
        cmbMetodo.setMaxWidth(Double.MAX_VALUE);

        cmbMetodo.valueProperty().addListener((obs, anterior, nuevo) -> {
            btnConfirmar.setDisable(
                    nuevo == null
                    || mesaActual == null
                    || ordenes.getNumeroOrden(mesaActual) == null
                    || ordenes.getLineasCuenta(mesaActual).isEmpty()
            );
        });

        Label textoTotal = new Label("TOTAL A PAGAR");
        textoTotal.getStyleClass().add("total-texto");

        Region espacio = new Region();
        HBox.setHgrow(espacio, Priority.ALWAYS);

        HBox filaTotal = new HBox(12, textoTotal, espacio, lblTotal);
        filaTotal.setAlignment(Pos.CENTER_LEFT);
        filaTotal.getStyleClass().add("fila-total");

        btnConfirmar.getStyleClass().add("btn-rojo");
        btnConfirmar.setMaxWidth(Double.MAX_VALUE);
        btnConfirmar.setOnAction(e -> confirmarPago());

        btnVolver.getStyleClass().add("btn-borde");
        btnVolver.setMaxWidth(Double.MAX_VALUE);
        btnVolver.setOnAction(e -> alVolver.run());

        VBox panel = new VBox(
                18,
                titulo,
                new Separator(),
                lblOrden,
                lblMesa,
                textoMetodo,
                cmbMetodo,
                new Separator(),
                filaTotal,
                btnConfirmar,
                btnVolver
        );

        panel.setPadding(new Insets(30));
        panel.setMaxWidth(520);
        panel.getStyleClass().add("panel-orden");

        setCenter(panel);
    }

    private void confirmarPago() {
        if (mesaActual == null) {
            mostrarAlerta(
                    Alert.AlertType.WARNING,
                    "Cuenta no seleccionada",
                    "No hay una mesa seleccionada."
            );
            return;
        }

        Pago.Metodo metodo = cmbMetodo.getValue();

        if (metodo == null) {
            mostrarAlerta(
                    Alert.AlertType.WARNING,
                    "Método de pago",
                    "Selecciona un método de pago para continuar."
            );
            return;
        }

        Integer numeroOrden = ordenes.getNumeroOrden(mesaActual);
        int total = ordenes.getTotalCuenta(mesaActual);

        if (numeroOrden == null || ordenes.getLineasCuenta(mesaActual).isEmpty()) {
            mostrarAlerta(
                    Alert.AlertType.WARNING,
                    "Cuenta no disponible",
                    "La cuenta ya no está abierta o no contiene productos."
            );
            return;
        }

        String error = ordenes.cerrarCuenta(mesaActual);

        if (error != null) {
            mostrarAlerta(
                    Alert.AlertType.WARNING,
                    "No se pudo cerrar la cuenta",
                    error
            );
            return;
        }

        pagos.add(new Pago(numeroOrden, mesaActual, total, metodo));

        mostrarAlerta(
                Alert.AlertType.INFORMATION,
                "Pago registrado",
                "Orden #" + numeroOrden
                        + "\nMesa: " + mesaActual
                        + "\nMétodo de pago: " + metodo
                        + "\nTotal pagado: $ " + moneda.format(total)
        );

        mesaActual = null;
        alPagar.run();
    }

    private void mostrarAlerta(
            Alert.AlertType tipo, String titulo, String mensaje) {

        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);

        if (css != null && !css.isBlank()) {
            alerta.getDialogPane().getStylesheets().add(css);
        }

        alerta.showAndWait();
    }
}