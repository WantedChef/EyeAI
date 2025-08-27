package chef.sheesh.eyeAI.ui.admin;

import chef.sheesh.eyeAI.bootstrap.ChefAI;
import chef.sheesh.eyeAI.ui.AdminGui;
import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public final class EditPlayerGui {
    private final ChefAI plugin;

    public EditPlayerGui(ChefAI plugin) {
        this.plugin = plugin;
    }

    public void open(Player viewer) {
        ChestGui gui = new ChestGui(3, "Edit Player");
        StaticPane pane = new StaticPane(0, 0, 9, 3);

        // Placeholder: select player (future multi-depth navigation)
        pane.addItem(new GuiItem(named(new ItemStack(Material.PLAYER_HEAD), "Select Player"), event -> {
            event.setCancelled(true);
            event.getWhoClicked().sendMessage(Component.text("Player selection not implemented yet."));
        }), 2, 1);

        // Placeholder: edit attributes
        pane.addItem(new GuiItem(named(new ItemStack(Material.BOOK), "Edit Attributes"), event -> {
            event.setCancelled(true);
            event.getWhoClicked().sendMessage(Component.text("Attribute editor not implemented yet."));
        }), 4, 1);

        // Back to Admin
        pane.addItem(new GuiItem(named(new ItemStack(Material.ARROW), "Back"), event -> {
            event.setCancelled(true);
            new AdminGui(plugin).open((Player) event.getWhoClicked());
        }), 6, 1);

        gui.addPane(pane);
        gui.show(viewer);
    }

    private static ItemStack named(ItemStack stack, String name) {
        ItemMeta meta = stack.getItemMeta();
        meta.displayName(Component.text(name));
        stack.setItemMeta(meta);
        return stack;
    }
}
