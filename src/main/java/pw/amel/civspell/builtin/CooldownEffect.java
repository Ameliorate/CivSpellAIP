package pw.amel.civspell.builtin;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import pw.amel.civspell.spell.CastData;
import pw.amel.civspell.spell.Effect;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class CooldownEffect implements Effect {

    public CooldownEffect(ConfigurationSection config) {
        double cooldownMinutes = config.getDouble("cooldownMinutes", 0);
        double cooldownSeconds = config.getDouble("cooldownSeconds", 0);
        double cooldownTicks = config.getDouble("cooldownTicks", 0);

        this.cooldownTicks = (int) (cooldownTicks + (cooldownMinutes * 60 * 20) + (cooldownSeconds * 20));

        isFancy = config.getBoolean("fancy", true);
    }

    private int cooldownTicks;
    private boolean isFancy;

    private Set<UUID> onCooldown = new HashSet<>();

    @Override
    public void cast(CastData castData) {
        UUID playerUUID = castData.player.getUniqueId();
        if (onCooldown.contains(playerUUID)) {
            castData.returnCast();
            return;
        }

        if (isFancy) {
            BukkitRunnable runnable = new BukkitRunnable() {
                long progressed = -5;
                @Override
                public void run() {
                    progressed += 5;
                    if (progressed > cooldownTicks) {
                        castData.main.getServer().getScheduler().runTask(castData.main, this::cancel);
                        return;
                    }

                    Player player = castData.getNewPlayer();
                    if (player == null)
                        return;

                    if (!player.getInventory().getItemInMainHand().isSimilar(castData.triggerItem) &&
                            !player.getInventory().getItemInOffHand().isSimilar(castData.triggerItem))
                        return;

                    final int BAR_LENGTH = 60;

                    // generate progress bar
                    float percentComplete = progressed / (float) cooldownTicks;
                    int numberOfColons = (int) (percentComplete * BAR_LENGTH);
                    numberOfColons++; // fix bar never filling up 100%
                    int numberOfSpaces = BAR_LENGTH - numberOfColons;

                    StringBuilder str = new StringBuilder();
                    for (int i = 0; i < numberOfColons; i++) {
                        str.append("|");
                    }
                    StringBuilder str2 = new StringBuilder();
                    for (int i = 0; i < numberOfSpaces; i++) {
                        str2.append("|");
                    }

                    TextComponent progressBar = new TextComponent("[");
                    TextComponent midPart = new TextComponent(str.toString());
                    midPart.setColor(ChatColor.AQUA);
                    progressBar.addExtra(midPart);
                    TextComponent spacesPart = new TextComponent(str2.toString());
                    spacesPart.setColor(ChatColor.GRAY);
                    midPart.addExtra(spacesPart);
                    TextComponent endPart = new TextComponent("]");
                    endPart.setColor(ChatColor.WHITE);
                    spacesPart.addExtra(endPart);

                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, progressBar);
                }
            };
            runnable.runTaskTimer(castData.main, 0, 5);
        }

        onCooldown.add(playerUUID);
        castData.main.getServer().getScheduler().runTaskLater(castData.main, () -> onCooldown.remove(playerUUID), cooldownTicks);
    }
}
