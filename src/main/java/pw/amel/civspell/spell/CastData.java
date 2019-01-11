package pw.amel.civspell.spell;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import pw.amel.civspell.CivSpells;

import java.util.HashSet;

public class CastData {
    /**
     * The player that casted this spell.
     */
    public Player player;

    /**
     * If the spell was casted by clicking on a block, this will be set to the block that was clicked.
     *
     * This will be null if the spell was casted by clicking in the air.
     */
    public Block blockClicked;

    /**
     * If the spell was casted by clicking it on a block, this is the face that was clicked.
     */
    public BlockFace blockClickedFace;

    /**
     * The action that was used to cast this spell, right or left click.
     *
     * This can not be Action.PHYSICAL
     */
    public Action castAction;

    public CivSpells main;

    /**
     * The name of the spell that is being cast.
     */
    public String spellName;

    /**
     * The item that was in the hand of the caster player when this spell was casted.
     */
    public ItemStack triggerItem;

    public boolean isRightClick() {
        return castAction == Action.RIGHT_CLICK_AIR || castAction == Action.RIGHT_CLICK_BLOCK;
    }

    public boolean isLeftClick() {
        return castAction == Action.LEFT_CLICK_AIR || castAction == Action.LEFT_CLICK_BLOCK;
    }

    /**
     * @return True if the spell was casted by clicking it on a block, false if it was casted by clicking in the air.
     */
    public boolean isBlockClickCasted() {
        return blockClicked != null;
    }

    private boolean alreadyReturned = false;
    private HashSet<Runnable> returnHooks = new HashSet<>();

    /**
     * If the spell has failed in some way or can not be casted, the Runnable will be ran, giving the oppourtunity to
     * return the costs of the spell and revert any of its results.
     * @param onReturn A Runnable that will be ran if the spell has failed.
     */
    public void addReturnHook(Runnable onReturn) {
        returnHooks.add(onReturn);
    }

    public boolean isAlreadyReturned() {
        return alreadyReturned;
    }

    /**
     * If it has been determined that the spell has failed or is otherwise unable to be casted, return the costs of
     * casting this spell and undo any effects that can be reverted.
     *
     * @return If the spell has already been returned.
     */
    public boolean returnCast() {
        if (alreadyReturned)
            return true;

        for (Runnable hook : returnHooks) {
            hook.run();
        }

        alreadyReturned = true;
        return false;
    }
}
