package pw.amel.civspell.gameplay;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import pw.amel.civspell.CastHelper;
import pw.amel.civspell.Main;
import pw.amel.civspell.SpellCastMethod;

public class Scroll implements Listener {
    public Scroll(Main mainPlugin, String formatString) {
        this.mainPlugin = mainPlugin;
        String[] fs = formatString.split("\\{NAME}");
        assert fs.length == 1 || fs.length == 2;
        this.formatStringPrefix = fs[0];
        if (fs.length == 2) {
            this.formatStringPostfix = fs[1];
        } else {
            this.formatStringPostfix = "";
        }
    }

    private Main mainPlugin;
    private String formatStringPrefix;
    private String formatStringPostfix;

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Empty hand or vanilla sugar cane where it can't be placed.
        if (event.getItem() == null) {
            return;
        }
        // Item with no meta.
        else if (event.getItem().getItemMeta() == null) {
            System.out.println("awjrkaht");
            return;
        }
        // Check for default name.
        else if (event.getItem().getItemMeta().getDisplayName() == null) {
            return;
        }
        // Check if it's a scroll.
        else if (!(event.getMaterial() == Material.SUGAR_CANE) ||
                !(event.getItem().getItemMeta().getDisplayName().startsWith(formatStringPrefix) &&
                        event.getItem().getItemMeta().getDisplayName().endsWith(formatStringPostfix))) {
            return;
        }
        // Make it not trigger stepping on redstone.
        else if (event.getAction() == Action.PHYSICAL) {
            return;
        }

        // Parse spellName out of the item name.
        String spellName = event.getItem().getItemMeta().getDisplayName().replaceAll("^" + formatStringPrefix, "");
        spellName = spellName.replaceAll(formatStringPostfix + "$", "");

        boolean result = CastHelper.castSpell(spellName, event.getPlayer(), SpellCastMethod.SCROLL,
                event.getHand() == EquipmentSlot.HAND, event.getClickedBlock(), event.getAction(),
                event.getBlockFace(), mainPlugin);

        if (result) {
            event.setCancelled(true);
        }
    }
}
