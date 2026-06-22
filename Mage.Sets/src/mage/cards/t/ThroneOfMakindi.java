package mage.cards.t;

import mage.abilities.Ability;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.costs.common.RemoveCountersSourceCost;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.costs.mana.GenericManaCost;
import mage.abilities.effects.common.counter.AddCountersSourceEffect;
import mage.abilities.mana.ColorlessManaAbility;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.abilities.mana.conditional.FilteredSpellManaCondition;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.counters.CounterType;
import mage.filter.FilterTyped;
import mage.filter.predicate.typed.Spell.object.KickedSpellPredicate;

import java.util.UUID;

/**
 * @author TheElk801
 */
public final class ThroneOfMakindi extends CardImpl {

    private static final FilterTyped filter = new FilterTyped("kicked spell")
            .add(KickedSpellPredicate.instance);

    public ThroneOfMakindi(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.LAND}, "");

        // {T}: Add {C}.
        this.addAbility(new ColorlessManaAbility());

        // {1}, {T}: Put a charge counter on Throne of Makindi.
        Ability ability = new SimpleActivatedAbility(
                new AddCountersSourceEffect(CounterType.CHARGE.createInstance()), new GenericManaCost(1)
        );
        ability.addCost(new TapSourceCost());
        this.addAbility(ability);

        // {T}, Remove a charge counter from Throne of Makindi: Add two mana of any one color. Spend this mana only to cast kicked spells.
        this.addAbility(ComposedManaAbilityBuilder.builder()
                .cost(new TapSourceCost())
                .cost(new RemoveCountersSourceCost(CounterType.CHARGE.createInstance()))
                .addAnyColor(2)
                .condition(new FilteredSpellManaCondition(filter))
                .ruleText("Add two mana of any one color. Spend this mana only to cast kicked spells")
                .build()
        );
    }

    private ThroneOfMakindi(final ThroneOfMakindi card) {
        super(card);
    }

    @Override
    public ThroneOfMakindi copy() {
        return new ThroneOfMakindi(this);
    }
}
