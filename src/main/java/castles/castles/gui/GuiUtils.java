package castles.castles.gui;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class GuiUtils {
    public static ItemStack createGuiItem(final Material material, final int customModelData, final Component name, final Component... lore) {
        final ItemStack item = new ItemStack(material, 1);
        final ItemMeta meta = item.getItemMeta();

        meta.displayName(name);

        meta.lore(Arrays.asList(lore));

        if (customModelData != 0) meta.setCustomModelData(1);

        item.setItemMeta(meta);

        return item;
    }

    public static ItemStack createGuiItem(final Material material, final Component name, final Component... lore) {
        return createGuiItem(material, 0, name, lore);
    }
}