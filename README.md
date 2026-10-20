# Carbonífero - Prototipo 3: Ordenar platos por mesa (RF-04)

Cascarón en Java + JavaFX para mostrar avance al cliente. Los datos son de ejemplo y viven en memoria (no hay base de datos).

## Requisitos
- JDK 17 o superior
- Maven 3.8 o superior (descarga JavaFX automáticamente)

## Ejecutar
```
mvn clean javafx:run
```
También se puede abrir como proyecto Maven en IntelliJ IDEA, NetBeans o Eclipse y ejecutar `co.unillanos.carbonifero.Main`.

## Qué muestra
1. Selección de mesa (libres en blanco, ocupadas en rojo).
2. 4 platos con disponibilidad según el inventario de ejemplo, y 5 adicionales.
3. Orden actual con cantidades, total, confirmar y cancelar.
4. Al confirmar: se descuenta el inventario, la mesa pasa a ocupada y aparece la alerta de stock mínimo (quedan 2 o menos).
5. Una mesa ocupada recibe nuevos platos en la misma orden abierta.

## Datos de ejemplo (Main.cargarDatos)
Platos: Carne a la llanera, Costillas BBQ, Pechuga a la parrilla, Hamburguesa Carbonífero.
Adicionales: Patacón, Yuca frita, Papa criolla, Aguacate, Huevo frito.
Colores: blanco, negro (#111111) y rojo (#C8102E) en `src/main/resources/styles.css`.
