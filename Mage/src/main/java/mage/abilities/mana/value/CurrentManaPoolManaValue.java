package mage.abilities.mana.value;

import mage.Mana;
import mage.abilities.Ability;
import mage.abilities.effects.Effect;
import mage.abilities.mana.ComposedManaEffect;
import mage.constants.ManaType;
import mage.game.Game;
import mage.players.Player;
import mage.util.CardUtil;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Produces mana based on current mana in pool.
 * Used for effects like Doubling Cube that add mana equal to the current pool.
 */
public class CurrentManaPoolManaValue implements ManaValue {

    private final int multiplier;

    public CurrentManaPoolManaValue() {
        this(1);
    }

    public CurrentManaPoolManaValue(int multiplier) {
        this.multiplier = multiplier;
    }

    @Override
    public List<Mana> evaluate(Game game, Ability source, Effect manaEffect, boolean produceMana) {
        if (game == null || multiplier <= 0) {
            return Collections.emptyList();
        }

        Mana mana = produceMana
                ? getManaFromActualPool(game, source, manaEffect)
                : getReferenceMana(game, source, manaEffect);
        if (mana == null || mana.count() <= 0) {
            return Collections.emptyList();
        }

        return Collections.singletonList(scaleMana(mana, multiplier));
    }

    private Mana getReferenceMana(Game game, Ability source, Effect manaEffect) {
        if (manaEffect instanceof ComposedManaEffect composedManaEffect) {
            Mana possibleManaInPool = composedManaEffect.getPossibleManaInPool();
            if (possibleManaInPool != null) {
                return sanitizeMana(possibleManaInPool);
            }
        }
        return getManaFromActualPool(game, source, manaEffect);
    }

    private Mana getManaFromActualPool(Game game, Ability source, Effect manaEffect) {
        Player player = getManaPlayer(game, source, manaEffect);
        if (player == null) {
            return null;
        }

        return sanitizeMana(player.getManaPool().getMana());
    }

    private Player getManaPlayer(Game game, Ability source, Effect manaEffect) {
        if (manaEffect instanceof ComposedManaEffect composedManaEffect) {
            return composedManaEffect.getManaPlayer(game, source);
        }
        return game.getPlayer(source.getControllerId());
    }

    private static Mana sanitizeMana(Mana mana) {
        return new Mana(
                mana.getWhite(),
                mana.getBlue(),
                mana.getBlack(),
                mana.getRed(),
                mana.getGreen(),
                0,
                mana.getAny(),
                mana.getColorless()
        );
    }

    private static Mana scaleMana(Mana mana, int multiplier) {
        return new Mana(
                CardUtil.overflowMultiply(mana.getWhite(), multiplier),
                CardUtil.overflowMultiply(mana.getBlue(), multiplier),
                CardUtil.overflowMultiply(mana.getBlack(), multiplier),
                CardUtil.overflowMultiply(mana.getRed(), multiplier),
                CardUtil.overflowMultiply(mana.getGreen(), multiplier),
                0,
                CardUtil.overflowMultiply(mana.getAny(), multiplier),
                CardUtil.overflowMultiply(mana.getColorless(), multiplier)
        );
    }

    @Override
    public Set<ManaType> getProducibleTypes() {
        return EnumSet.noneOf(ManaType.class);
    }

    @Override
    public Set<ManaType> getProducibleTypes(Game game, Ability source, Effect manaEffect) {
        Mana mana = game == null ? null : getReferenceMana(game, source, manaEffect);
        return mana == null || mana.count() <= 0
                ? EnumSet.noneOf(ManaType.class)
                : ManaType.getManaTypesFromManaList(mana);
    }

    @Override
    public CurrentManaPoolManaValue copy() {
        return new CurrentManaPoolManaValue(multiplier);
    }

    @Override
    public String toString() {
        return multiplier == 1 ? "current mana pool" : multiplier + " * current mana pool";
    }
}
