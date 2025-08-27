package chef.sheesh.eyeAI.ui;

import chef.sheesh.eyeAI.bootstrap.ChefAI;
import org.bukkit.entity.Player;
import chef.sheesh.eyeAI.infra.diagnostic.Diagnostic;
import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public final class AdminGui {

    public AdminGui(ChefAI plugin) {
        // Plugin parameter kept for future use
    }

    public void open(Player player) {
        ChestGui gui = new ChestGui(3, "CHEF-AI Admin");
        StaticPane pane = new StaticPane(0, 0, 9, 3);

        // TPS display
        pane.addItem(new GuiItem(named(new ItemStack(Material.CLOCK),
                "TPS: " + String.format("%.2f", Diagnostic.getTps())), event -> event.setCancelled(true)), 1, 1);

        // Simple placeholder items
        pane.addItem(new GuiItem(named(new ItemStack(Material.ARMOR_STAND),
                "Player Tools"), event -> {
            event.setCancelled(true);
        }), 3, 1);

        pane.addItem(new GuiItem(named(new ItemStack(Material.REDSTONE),
                "Settings"), event -> {
            event.setCancelled(true);
        }), 5, 1);

        gui.addPane(pane);
        gui.show(player);
    }

    private static ItemStack named(ItemStack stack, String name) {
        ItemMeta meta = stack.getItemMeta();
        meta.displayName(Component.text(name));
        stack.setItemMeta(meta);
        return stack;
    }
}
