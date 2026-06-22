package mage.cards.i;

import mage.abilities.common.SpellCastControllerTriggeredAbility;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.costs.mana.GenericManaCost;
import mage.abilities.effects.common.GainLifeEffect;
import mage.abilities.mana.ColorlessManaAbility;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.abilities.mana.conditional.FilteredSpellManaCondition;
import mage.abilities.mana.value.TwoDifferentColorsManaValue;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.filter.FilterSpell;
import mage.filter.FilterTyped;
import mage.filter.predicate.typed.Spell.SpellPredicate;

import java.util.UUID;

/**
 * @author TheElk801
 */
public final class InterplanarBeacon extends CardImpl {

    private static final FilterSpell filter = new FilterSpell("a planeswalker spell");
    private static final FilterTyped manaFilter = new FilterTyped("a planeswalker spell")
            .addAll(
                    SpellPredicate.instance,
                    CardType.PLANESWALKER.getPredicate()
            );
    static {
        filter.add(CardType.PLANESWALKER.getPredicate());
    }

    public InterplanarBeacon(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.LAND}, "");

        // Whenever you cast a planeswalker spell, you gain 1 life.
        this.addAbility(new SpellCastControllerTriggeredAbility(
                new GainLifeEffect(1), filter, false
        ));

        // {T}: Add {C}.
        this.addAbility(new ColorlessManaAbility());

        // {1}, {T}: Add two mana of different colors. Spend this mana only to cast planeswalker spells.
        this.addAbility(ComposedManaAbilityBuilder.builder()
                .cost(new GenericManaCost(1))
                .cost(new TapSourceCost())
                .addManaValue(new TwoDifferentColorsManaValue())
                .condition(new FilteredSpellManaCondition(manaFilter))
                .ruleText("Add two mana of different colors. Spend this mana only to cast planeswalker spells")
                .build()
        );
    }

    private InterplanarBeacon(final InterplanarBeacon card) {
        super(card);
    }

    @Override
    public InterplanarBeacon copy() {
        return new InterplanarBeacon(this);
    }
}
