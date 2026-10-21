# Carbonífero - Prototipo integrado

Une en una sola aplicación JavaFX los dos prototipos del grupo:

1. **Ordenar platos por mesa** (RF-04): prototipo original de tu compañero.
2. **Gestión de recetas**: crear platos y definir sus ingredientes con la cantidad por porción.

Los datos son de ejemplo y viven en memoria (no hay base de datos).

## Requisitos
- JDK 17 o superior
- Maven 3.8 o superior (descarga JavaFX automáticamente; la primera vez necesita internet)

## Ejecutar
```
mvn clean javafx:run
```
También se puede abrir como proyecto Maven en IntelliJ IDEA, NetBeans o Eclipse y ejecutar `co.unillanos.carbonifero.Main`.

Para comprobar que todo funciona, sigue **GUIA_VERIFICACION.md**.

## Cómo se conectan los dos módulos
- Los dos usan los mismos datos (`Datos`): inventario de ingredientes, platos con receta y adicionales.
- La disponibilidad de un plato ya no es un número fijo: se calcula con el inventario y su receta
  (porciones posibles = lo que alcance del ingrediente más escaso).
- Al confirmar una orden se descuentan del inventario los ingredientes de cada plato.
- Si se crea o edita un plato en Gestión de recetas, el cambio se refleja al volver a Ordenar platos.

## Estructura
```
pom.xml
src/main/java/co/unillanos/carbonifero/
  Main.java          Ventana principal con navegación entre módulos
  OrdenesView.java   Módulo Ordenar platos (prototipo original, conectado a recetas)
  RecetasView.java   Módulo Gestión de recetas
  Datos.java         Datos de ejemplo compartidos (inventario, platos, adicionales)
  Item.java          Plato o adicional; los platos tienen receta
  LineaOrden.java    Renglón de una orden (sin cambios)
  LineaReceta.java   Ingrediente + cantidad por porción
  Ingrediente.java   Ingrediente del inventario
src/main/resources/styles.css   Estilos (paleta blanco, negro y rojo)
```

## Cambios respecto a los prototipos originales
- `Main` del prototipo de órdenes pasó a `OrdenesView`; el nuevo `Main` solo arma la ventana y la navegación.
- `Item` ya no guarda un número de porciones: las calcula con la receta. Su constructor perdió ese parámetro.
- Los datos de ejemplo se movieron de `Main.cargarDatos` a `Datos`. El inventario de ingredientes fue calculado
  para que los platos arranquen con las mismas porciones de antes: 12, 3, 8 y 6.
- Al agregar platos a una orden se tiene en cuenta el inventario completo del carrito
  (dos platos que comparten ingrediente ya no pueden pasarse del stock).
