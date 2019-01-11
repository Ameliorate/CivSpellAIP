package pw.amel.civspell.spell;

public interface Effect {
    /*
    public Effect(ConfigurationSection config);
    // This must be there in order for the Effect to properly work, however java doesn't allow interfaces to
    // require the presence of a constructor. As a result, this is commented out.
     */

    /**
     * Cast this spell implementation with the given CastData.
     *
     * This should not be used directly. Instead see CastHelper.castSpell.
     * @param castData The set of data that the spell can use to influince its casting.
     */
    void cast(CastData castData);
}
