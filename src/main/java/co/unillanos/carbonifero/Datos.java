package co.unillanos.carbonifero;

import java.util.ArrayList;
import java.util.List;

/**
 * Datos de ejemplo compartidos por los dos módulos (en memoria, sin base de datos).
 * Cuando exista el módulo de inventario real, esto se reemplaza por sus datos.
 */
public class Datos {

    public static final int STOCK_MINIMO = 2;

    private final List<Ingrediente> inventario = new ArrayList<>();
    private final List<Item> platos = new ArrayList<>();
    private final List<Item> adicionales = new ArrayList<>();

    public Datos() {
        // Inventario de ejemplo
        Ingrediente carne = new Ingrediente("Carne de res", "g", 5000);
        Ingrediente costillas = new Ingrediente("Costillas de cerdo", "g", 1200);
        Ingrediente pechuga = new Ingrediente("Pechuga de pollo", "g", 2000);
        Ingrediente yuca = new Ingrediente("Yuca", "g", 1800);
        Ingrediente papa = new Ingrediente("Papa", "g", 6000);
        Ingrediente ensalada = new Ingrediente("Ensalada mixta", "g", 2000);
        Ingrediente salsaBbq = new Ingrediente("Salsa BBQ", "ml", 1500);
        Ingrediente arroz = new Ingrediente("Arroz", "g", 5000);
        Ingrediente pan = new Ingrediente("Pan de hamburguesa", "unidad", 20);
        Ingrediente queso = new Ingrediente("Queso", "g", 240);
        Ingrediente tocineta = new Ingrediente("Tocineta", "g", 500);
        Ingrediente aceite = new Ingrediente("Aceite", "ml", 3000);
        Ingrediente sal = new Ingrediente("Sal", "g", 10);
        inventario.addAll(List.of(carne, costillas, pechuga, yuca, papa, ensalada, salsaBbq,
                arroz, pan, queso, tocineta, aceite, sal));

        // Platos con su receta (por porción). Con este inventario salen las mismas
        // disponibilidades del prototipo de órdenes: 12, 3, 8 y 6 porciones.
        Item llanera = new Item("Carne a la llanera", "Carne asada al carbón con yuca, papa y ensalada.", 38000, Item.Tipo.PLATO);
        llanera.getReceta().addAll(List.of(new LineaReceta(carne, 300), new LineaReceta(yuca, 150),
                new LineaReceta(papa, 150), new LineaReceta(ensalada, 80)));

        Item costillasBbq = new Item("Costillas BBQ", "Costillas ahumadas con salsa BBQ de la casa.", 42000, Item.Tipo.PLATO);
        costillasBbq.getReceta().addAll(List.of(new LineaReceta(costillas, 400), new LineaReceta(salsaBbq, 60)));

        Item pechugaPlato = new Item("Pechuga a la parrilla", "Pechuga marinada con arroz y ensalada fresca.", 32000, Item.Tipo.PLATO);
        pechugaPlato.getReceta().addAll(List.of(new LineaReceta(pechuga, 250), new LineaReceta(arroz, 120),
                new LineaReceta(ensalada, 80)));

        Item hamburguesa = new Item("Hamburguesa Carbonífero", "Carne de res, queso, tocineta y papas a la francesa.", 28000, Item.Tipo.PLATO);
        hamburguesa.getReceta().addAll(List.of(new LineaReceta(carne, 180), new LineaReceta(pan, 1),
                new LineaReceta(queso, 40), new LineaReceta(tocineta, 30), new LineaReceta(papa, 200)));

        platos.addAll(List.of(llanera, costillasBbq, pechugaPlato, hamburguesa));

        adicionales.add(new Item("Patacón", "", 6000, Item.Tipo.ADICIONAL));
        adicionales.add(new Item("Yuca frita", "", 5000, Item.Tipo.ADICIONAL));
        adicionales.add(new Item("Papa criolla", "", 5000, Item.Tipo.ADICIONAL));
        adicionales.add(new Item("Aguacate", "", 4000, Item.Tipo.ADICIONAL));
        adicionales.add(new Item("Huevo frito", "", 3000, Item.Tipo.ADICIONAL));
    }

    public List<Ingrediente> getInventario() { return inventario; }
    public List<Item> getPlatos() { return platos; }
    public List<Item> getAdicionales() { return adicionales; }
}
