# Carbonífero - Prototipo integrado

Une en una sola aplicación JavaFX los prototipos del grupo:

1. **Ordenar platos por mesa** (RF-04): prototipo original de tu compañero.
2. **Gestión de recetas**: crear platos y definir sus ingredientes con la cantidad por porción.
3. **Inventario de ingredientes** (CU-02 / RF-02 / RF-03): el administrador registra compras, ajusta existencias, crea insumos y ve alertas de stock mínimo.

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

## Cómo se conectan los tres módulos
- Los tres usan los mismos datos (`Datos`): inventario de ingredientes, platos con receta, adicionales y movimientos de inventario.
- La disponibilidad de un plato no es un número fijo: se calcula con el inventario y su receta
  (porciones posibles = lo que alcance del ingrediente más escaso).
- Al confirmar una orden se descuentan del inventario los ingredientes de cada plato y se guarda un movimiento de **consumo** (fecha, hora y usuario Administrador).
- Si se crea o edita un plato en Gestión de recetas, el cambio se refleja al volver a Ordenar platos.
- Si se compra, ajusta o crea un ingrediente en Inventario, al volver a Ordenar platos o Recetas se recalcula la disponibilidad. Si la existencia llega a cero, los platos que dependen de ese insumo quedan deshabilitados.

## Inventario (RF-02 / RF-03)
- Cada ingrediente muestra existencia actual, unidad de medida y stock mínimo.
- Acciones: registrar una **compra**, **ajustar** existencias (con motivo obligatorio) o **crear** un ingrediente nuevo.
- Validaciones: cantidades mayores a cero; un ajuste no puede dejar la existencia en negativo.
- Si la existencia es menor o igual al stock mínimo, aparece una alerta visual (RF-03). Si es cero, el insumo se marca como agotado.
- En los datos de ejemplo, **Sal** arranca en 10 g con mínimo 50 g para ver la alerta al abrir el módulo.

## Estructura
```
pom.xml
src/main/java/co/unillanos/carbonifero/
  Main.java                  Ventana principal con navegación entre módulos
  OrdenesView.java           Módulo Ordenar platos (conectado a recetas e inventario)
  RecetasView.java           Módulo Gestión de recetas
  InventarioView.java        Módulo Inventario (compras, ajustes, altas y alertas)
  Datos.java                 Datos de ejemplo y reglas de inventario
  Item.java                  Plato o adicional; los platos tienen receta
  LineaOrden.java            Renglón de una orden
  LineaReceta.java           Ingrediente + cantidad por porción
  Ingrediente.java           Insumo (stock, unidad, stock mínimo)
  MovimientoInventario.java  Historial de compra, ajuste, consumo y alta
src/main/resources/styles.css   Estilos (paleta blanco, negro y rojo)
```

## Cambios respecto a los prototipos originales
- `Main` del prototipo de órdenes pasó a `OrdenesView`; el nuevo `Main` arma la ventana y la navegación (órdenes, recetas e inventario).
- `Item` ya no guarda un número de porciones: las calcula con la receta. Su constructor perdió ese parámetro.
- Los datos de ejemplo se movieron de `Main.cargarDatos` a `Datos`. El inventario de ingredientes fue calculado
  para que los platos arranquen con las mismas porciones de antes: 12, 3, 8 y 6.
- Al agregar platos a una orden se tiene en cuenta el inventario completo del carrito
  (dos platos que comparten ingrediente ya no pueden pasarse del stock).
- `Ingrediente` incluye stock mínimo y estado de alerta/agotado. `Datos` registra movimientos y aplica compras, ajustes, altas y el consumo al confirmar una orden.
