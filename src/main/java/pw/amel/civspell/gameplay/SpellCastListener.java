package pw.amel.civspell.gameplay;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import pw.amel.civspell.CivSpells;
import pw.amel.civspell.SpellConfig;
import pw.amel.civspell.spell.CastHelper;

import java.util.stream.Collectors;

public class SpellCastListener implements Listener {
    public SpellCastListener(CivSpells mainPlugin) {
        this.mainPlugin = mainPlugin;
    }

    private CivSpells mainPlugin;

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Empty hand or vanilla sugar cane where it can't be placed.
        if (event.getItem() == null)
            return;
        // Stepping on redstone
        if (event.getAction() == Action.PHYSICAL)
            return;
        if (mainPlugin.config.spells.values().stream()
                .noneMatch(template -> template.itemExpression.matches(event.getItem())))
            return;

        SpellConfig.SpellData spellData = mainPlugin.config.spells.values().stream()
                .filter(template -> template.itemExpression.matches(event.getItem()))
                .collect(Collectors.toList())
                .get(0);

        if (!spellData.rightClickCast && (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR))
            return;
        if (!spellData.leftClickCast && (event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_AIR))
            return;

        CastHelper.castSpell(spellData, event.getPlayer(), event.getClickedBlock(),
                event.getAction(), event.getBlockFace(), event.getItem(), mainPlugin);

        event.setCancelled(true);
    }
}
