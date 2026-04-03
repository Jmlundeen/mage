package mage.abilities.mana.value;

import mage.Mana;
import mage.abilities.Ability;
import mage.abilities.effects.Effect;
import mage.constants.ManaType;
import mage.game.Game;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * A special mana value that produces "any color" mana.
 * This mana can be spent as any of the five colors.
 */
public class AnyColorManaValue implements ManaValue {

    private final int amount;

    public AnyColorManaValue(int amount) {
        this.amount = amount;
    }

    public AnyColorManaValue() {
        this(1);
    }

    @Override
    public List<Mana> evaluate(Game game, Ability source, Effect manaEffect) {
        if (amount <= 0) {
            return Collections.emptyList();
        }
        return Collections.singletonList(Mana.AnyMana(amount));
    }

    @Override
    public Set<ManaType> getProducibleTypes() {
        return EnumSet.of(ManaType.WHITE, ManaType.BLUE, ManaType.BLACK, ManaType.RED, ManaType.GREEN);
    }

    @Override
    public boolean isStatic() {
        return true;
    }

    public int getAmount() {
        return amount;
    }

    @Override
    public AnyColorManaValue copy() {
        return new AnyColorManaValue(amount);
    }

    @Override
    public String toString() {
        return "{" + amount + " any color}";
    }
}
