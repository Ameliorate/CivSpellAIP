package pw.amel.civspell.builtin;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import pw.amel.civspell.spell.CastData;
import pw.amel.civspell.spell.Effect;

import java.util.HashMap;
import java.util.Map;

public class RemoveTriggerItemEffect implements Effect {
    public RemoveTriggerItemEffect(ConfigurationSection config) {
        removeInCreativeMode = config.getBoolean("removeInCreativeMode", false);
        removeFromOnlyHands = config.getBoolean("removeFromOnlyHands", true);
        quantity = config.getInt("quantity", 1);
    }

    private boolean removeInCreativeMode;
    private boolean removeFromOnlyHands;
    private int quantity;

    @Override
    public void cast(CastData castData) {
        if (!removeInCreativeMode && castData.player.getGameMode() == GameMode.CREATIVE)
            return;

        ItemStack removeStack = castData.triggerItem.clone();
        removeStack.setAmount(quantity);

        if (removeFromOnlyHands) {
            boolean result = removeFromEitherMainOrOffHand(removeStack, castData.player.getInventory());

            if (!result) {
                castData.returnCast();
            }
        } else {
            ItemStack removeStack1 = removeStack.clone(); // see the javadoc of Inventory.removeItem().
            HashMap<Integer, ItemStack> result = castData.player.getInventory().removeItem(removeStack1);

            if (!result.isEmpty()) {
                for (Map.Entry<Integer, ItemStack> entry : result.entrySet()) {
                    entry.getValue().setAmount(quantity - entry.getValue().getAmount());
                    castData.player.getInventory().setItem(entry.getKey(), entry.getValue());

                    // I'd like to take this moment to personally insult this small piece of the Bukkit API.
                    // While most of the API is at least mediocre (see a dictonary for what I mean by this),
                    // this specific function, Inventory.removeItem(ItemStack item), is absolutely terrible.
                    // It is incredibly easy to misuse this function and result in dupe bugs and returning more
                    // items then intended.
                    //
                    // --------------------------------------------------------------------- //
                    //                                                                       //
                    // I call it right now. A dupe bug will be found inside this else block. //
                    //                                                                       //
                    // --------------------------------------------------------------------- //
                    //
                    // Let's take a look at a snippet from the javadoc for Inventory.removeItem(ItemStack item).
                    //
                    //   It is known that in some implementations this method will also set the inputted argument amount
                    //   to the number of that item not removed from slots.
                    //
                    // What does that even mean? Does this implementation do it? I don't want to read the source code,
                    // but now I have to in order to continue this rant. Alright I'm back. I can't read the
                    // code, because it's an interface. Thanks Java. But in that time, the relization came to me:
                    // What could have been meant by that snippet was that some Inventories set the argument amount,
                    // while others do not. In the same codebase, at the same version. What even is that?
                }

                castData.returnCast();
            }
        }

        if (!castData.isAlreadyReturned()) {
            castData.addReturnHook(() -> castData.player.getInventory().addItem(removeStack));
        }
    }

    /**
     * Removes the given itemstack from either the player's main hand or their offhand, preferring mainhand first.
     * @return If there were enough items of the right kind to remove. If this is false, no items have been removed from the player's inventory.
     */
    private static boolean removeFromEitherMainOrOffHand(ItemStack item, PlayerInventory player) {
        ItemStack mainHandItem = player.getItemInMainHand();
        ItemStack offHandItem = player.getItemInOffHand();
        if (mainHandItem.getAmount() < item.getAmount() && offHandItem.getAmount() < item.getAmount()) {
            return false;
        } else if (mainHandItem.isSimilar(item)) {
            if (mainHandItem.getAmount() == item.getAmount()) {
                player.setItemInMainHand(new ItemStack(Material.AIR));
                return true;
            } else {
                mainHandItem.setAmount(mainHandItem.getAmount() - item.getAmount());
                player.setItemInMainHand(mainHandItem);
                return true;
            }
        } else if (offHandItem.isSimilar(item)) {
            if (offHandItem.getAmount() == item.getAmount()) {
                player.setItemInOffHand(new ItemStack(Material.AIR));
                return true;
            } else {
                offHandItem.setAmount(offHandItem.getAmount() - item.getAmount());
                player.setItemInOffHand(offHandItem);
                return true;
            }
        } else {
            return false;
        }
    }
}
