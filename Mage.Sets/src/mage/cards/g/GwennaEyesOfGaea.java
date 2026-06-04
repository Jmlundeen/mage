package mage.cards.g;

import mage.MageInt;
import mage.MageObject;
import mage.abilities.Ability;
import mage.abilities.common.SpellCastControllerTriggeredAbility;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.effects.common.UntapSourceEffect;
import mage.abilities.effects.common.counter.AddCountersSourceEffect;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.abilities.mana.conditional.SpendOrActivateManaCondition;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.ComparisonType;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.counters.CounterType;
import mage.filter.FilterObject;
import mage.filter.StaticTypedFilters;
import mage.filter.common.FilterCreatureSpell;
import mage.filter.predicate.mageobject.PowerPredicate;

import java.util.UUID;

/**
 *
 * @author weirddan455
 */
public final class GwennaEyesOfGaea extends CardImpl {

    private static final FilterCreatureSpell filter
            = new FilterCreatureSpell("a creature spell with power 5 or greater");
    private static final FilterObject<MageObject> creatureFilter = new FilterObject<>("creature");

    static {
        filter.add(new PowerPredicate(ComparisonType.MORE_THAN, 4));
        creatureFilter.add(CardType.CREATURE.getPredicate());
    }

    public GwennaEyesOfGaea(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{2}{G}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.ELF);
        this.subtype.add(SubType.DRUID);
        this.subtype.add(SubType.SCOUT);
        this.power = new MageInt(2);
        this.toughness = new MageInt(3);

        // {T}: Add two mana in any combination of colors. Spend this mana only to cast creature spells or activate abilities of a creature or creature card.
        this.addAbility(new ComposedManaAbilityBuilder()
                .cost(new TapSourceCost())
                .addAnyCombination(2)
                .condition(new SpendOrActivateManaCondition(StaticTypedFilters.A_CREATURE_CARD))
                .ruleText("Add two mana in any combination of colors. Spend this mana only to cast creature spells or activate abilities of a creature or creature card")
                .build()
        );

        // Whenever you cast a creature spell with power 5 or greater, put a +1/+1 counter on Gwenna, Eyes of Gaea and untap it.
        Ability ability = new SpellCastControllerTriggeredAbility(
                new AddCountersSourceEffect(CounterType.P1P1.createInstance()),
                filter, false
        );
        ability.addEffect(new UntapSourceEffect().setText("and untap it"));
        this.addAbility(ability);
    }

    private GwennaEyesOfGaea(final GwennaEyesOfGaea card) {
        super(card);
    }

    @Override
    public GwennaEyesOfGaea copy() {
        return new GwennaEyesOfGaea(this);
    }
}
