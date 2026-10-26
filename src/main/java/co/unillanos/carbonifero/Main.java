package co.unillanos.carbonifero;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

/**
 * Prototipo integrado de Carbonífero.
 * Une órdenes (RF-04), recetas e inventario (RF-02 / RF-03).
 * Los tres módulos comparten Datos: al confirmar una orden se descuenta el inventario,
 * al comprar o ajustar un insumo se recalcula la disponibilidad de los platos.
 * Integra órdenes, recetas, inventario y cierre de cuentas. 
*/
public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        String css = getClass().getResource("/styles.css").toExternalForm();

        // Datos compartidos por los módulos.
        Datos datos = new Datos();

        // Registro de pagos en memoria durante la ejecución.
        List<Pago> pagos = new ArrayList<>();

        // Módulos existentes.
        OrdenesView ordenes = new OrdenesView(datos, css);
        RecetasView recetas = new RecetasView(datos, css);
        InventarioView inventario = new InventarioView(datos, css);

        // Nuevos módulos de cierre de cuenta.
        CuentaView cuentas = new CuentaView(ordenes, css);
        PagoView pago = new PagoView(ordenes, pagos, css);

        ToggleGroup grupoNav = new ToggleGroup();

        ToggleButton navOrdenes =
                crearNav("Ordenar platos", grupoNav);
        ToggleButton navRecetas =
                crearNav("Gestión de recetas", grupoNav);
        ToggleButton navInventario =
                crearNav("Inventario", grupoNav);
        ToggleButton navCuentas =
                crearNav("Cerrar cuenta", grupoNav);

        BorderPane root = new BorderPane();

        root.setTop(crearEncabezado(
                navOrdenes,
                navRecetas,
                navInventario,
                navCuentas
        ));

        root.setCenter(ordenes);

        // Desde las cuentas pendientes se pasa a la pantalla de pago.
        cuentas.setAlSolicitarPago(mesa -> {
            pago.prepararPago(mesa);
            root.setCenter(pago);
        });

        // Regresar a las cuentas pendientes sin cerrar la cuenta.
        pago.setAlVolver(() -> {
            cuentas.refrescar();
            root.setCenter(cuentas);
        });

        // Después de un pago confirmado, actualizar las cuentas.
        pago.setAlPagar(() -> {
            cuentas.refrescar();
            root.setCenter(cuentas);
        });

        // Navegación principal.
        grupoNav.selectedToggleProperty().addListener(
                (obs, anterior, nuevo) -> {

                    if (nuevo == null) {
                        if (anterior != null) {
                            anterior.setSelected(true);
                        }
                        return;
                    }

                    if (nuevo == navOrdenes) {
                        ordenes.alActivar();
                        root.setCenter(ordenes);

                    } else if (nuevo == navRecetas) {
                        recetas.alActivar();
                        root.setCenter(recetas);

                    } else if (nuevo == navInventario) {
                        inventario.alActivar();
                        root.setCenter(inventario);

                    } else if (nuevo == navCuentas) {
                        cuentas.alActivar();
                        root.setCenter(cuentas);
                    }
                }
        );

        navOrdenes.setSelected(true);

        Scene scene = new Scene(root, 1280, 780);
        scene.getStylesheets().add(css);

        stage.setTitle("Carbonífero - Prototipo integrado");
        stage.setScene(scene);
        stage.show();
    }

    private ToggleButton crearNav(String texto, ToggleGroup grupo) {
        ToggleButton boton = new ToggleButton(texto);
        boton.setToggleGroup(grupo);
        boton.getStyleClass().add("nav-btn");
        return boton;
    }

    private HBox crearEncabezado(
            ToggleButton navOrdenes,
            ToggleButton navRecetas,
            ToggleButton navInventario,
            ToggleButton navCuentas) {

        Label marca = new Label("CARBONÍFERO");
        marca.getStyleClass().add("marca");

        Region espacio = new Region();
        HBox.setHgrow(espacio, Priority.ALWAYS);

        Label usuario = new Label("Administrador");
        usuario.getStyleClass().add("usuario");

        HBox header = new HBox(
                16,
                marca,
                navOrdenes,
                navRecetas,
                navInventario,
                navCuentas,
                espacio,
                usuario
        );

        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("header");

        return header;
    }
}