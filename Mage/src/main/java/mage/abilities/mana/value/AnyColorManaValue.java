package mage.abilities.mana.value;

import mage.Mana;
import mage.abilities.Ability;
import mage.abilities.effects.Effect;
import mage.choices.ChoiceColor;
import mage.constants.ManaType;
import mage.constants.Outcome;
import mage.game.Game;
import mage.players.Player;

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
    public List<Mana> evaluate(Game game, Ability source, Effect manaEffect, boolean produceMana) {
        if (amount <= 0) {
            return Collections.emptyList();
        }
        if (produceMana) {
            Player controller = getChoicePlayer(game, source, manaEffect);
            if (controller == null) {
                return Collections.emptyList();
            }
            String mes = String.format("Select a color of mana to add %d of it", this.amount);
            ChoiceColor choice = new ChoiceColor(true, mes, game.getObject(source));
            if (controller.choose(Outcome.PutManaInPool, choice, game)) {
                if (choice.getColor() != null) {
                    return Collections.singletonList(choice.getMana(amount));
                }
            }
        }
        int calculatedAmount = calculateAmount(game, source, manaEffect, produceMana, null, amount);
        if (calculatedAmount <= 0) {
            return Collections.emptyList();
        }
        return Collections.singletonList(Mana.AnyMana(calculatedAmount));
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
