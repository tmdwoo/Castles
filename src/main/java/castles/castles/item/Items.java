package castles.castles.item;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class Items {
    public static ItemStack getItemCore() {
        ItemStack core = new ItemStack(Material.BLAZE_SPAWN_EGG, 1);
        ItemMeta meta = core.getItemMeta();
        meta.displayName(Component.text("Build Castle", NamedTextColor.GOLD, TextDecoration.BOLD));
        meta.setCustomModelData(1);
        List<Component> lore = List.of(new TextComponent[]{Component.text("Right click to build a castle", NamedTextColor.GRAY)});
        meta.lore(lore);
        core.setItemMeta(meta);
        return core;
    }
}