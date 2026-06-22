package mage.abilities.mana.value;

import mage.Mana;
import mage.abilities.Ability;
import mage.abilities.condition.Condition;
import mage.abilities.effects.Effect;
import mage.constants.ManaType;
import mage.game.Game;

import java.util.List;
import java.util.Set;

public class ConditionalManaValue implements ManaValue {

    private final ManaValue trueMana;
    private final ManaValue falseMana;
    private final Condition condition;

    public ConditionalManaValue(ManaValue trueMana, ManaValue falseMana, Condition condition) {
        this.trueMana = trueMana;
        this.falseMana = falseMana;
        this.condition = condition;
    }

    protected ConditionalManaValue(final ConditionalManaValue manaProvider) {
        this.trueMana = manaProvider.trueMana.copy();
        this.falseMana = manaProvider.falseMana.copy();
        this.condition = manaProvider.condition;
    }

    @Override
    public List<Mana> evaluate(Game game, Ability source, Effect manaEffect, boolean produceMana) {
        return condition.apply(game, source) ? trueMana.evaluate(game, source, manaEffect, produceMana)
                : falseMana.evaluate(game, source, manaEffect, produceMana);
    }

    @Override
    public Set<ManaType> getProducibleTypes() {
        return falseMana.getProducibleTypes();
    }

    @Override
    public ManaValue copy() {
        return new ConditionalManaValue(this);
    }
}
