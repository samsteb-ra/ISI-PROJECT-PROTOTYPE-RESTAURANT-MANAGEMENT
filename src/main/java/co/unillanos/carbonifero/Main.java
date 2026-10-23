package co.unillanos.carbonifero;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.geometry.Pos;
import javafx.stage.Stage;

/**
 * Prototipo integrado de Carbonífero.
 * Une órdenes (RF-04), recetas e inventario (RF-02 / RF-03).
 * Los tres módulos comparten Datos: al confirmar una orden se descuenta el inventario,
 * al comprar o ajustar un insumo se recalcula la disponibilidad de los platos.
 */
public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        String css = getClass().getResource("/styles.css").toExternalForm();
        Datos datos = new Datos();

        OrdenesView ordenes = new OrdenesView(datos, css);
        RecetasView recetas = new RecetasView(datos, css);
        InventarioView inventario = new InventarioView(datos, css);

        ToggleGroup grupoNav = new ToggleGroup();
        ToggleButton navOrdenes = crearNav("Ordenar platos", grupoNav);
        ToggleButton navRecetas = crearNav("Gestión de recetas", grupoNav);
        ToggleButton navInventario = crearNav("Inventario", grupoNav);

        BorderPane root = new BorderPane();
        root.setTop(crearEncabezado(navOrdenes, navRecetas, navInventario));
        root.setCenter(ordenes);

        grupoNav.selectedToggleProperty().addListener((obs, anterior, nuevo) -> {
            if (nuevo == null) {
                if (anterior != null) {
                    anterior.setSelected(true); // siempre debe haber un módulo activo
                }
                return;
            }
            if (nuevo == navOrdenes) {
                ordenes.alActivar();
                root.setCenter(ordenes);
            } else if (nuevo == navRecetas) {
                recetas.alActivar();
                root.setCenter(recetas);
            } else {
                inventario.alActivar();
                root.setCenter(inventario);
            }
        });
        navOrdenes.setSelected(true);

        Scene scene = new Scene(root, 1280, 780);
        scene.getStylesheets().add(css);

        stage.setTitle("Carbonífero - Prototipo integrado");
        stage.setScene(scene);
        stage.show();
    }

    private ToggleButton crearNav(String texto, ToggleGroup grupo) {
        ToggleButton b = new ToggleButton(texto);
        b.setToggleGroup(grupo);
        b.getStyleClass().add("nav-btn");
        return b;
    }

    private HBox crearEncabezado(ToggleButton navOrdenes, ToggleButton navRecetas, ToggleButton navInventario) {
        Label marca = new Label("CARBONÍFERO");
        marca.getStyleClass().add("marca");
        Region espacio = new Region();
        HBox.setHgrow(espacio, Priority.ALWAYS);
        Label usuario = new Label("Administrador");
        usuario.getStyleClass().add("usuario");

        HBox header = new HBox(16, marca, navOrdenes, navRecetas, navInventario, espacio, usuario);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("header");
        return header;
    }
}
