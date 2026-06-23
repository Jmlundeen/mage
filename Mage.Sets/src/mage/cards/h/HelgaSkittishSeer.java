package mage.cards.h;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.SpellCastControllerTriggeredAbility;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.dynamicvalue.common.SourcePermanentPowerValue;
import mage.abilities.effects.common.DrawCardSourceControllerEffect;
import mage.abilities.effects.common.GainLifeEffect;
import mage.abilities.effects.common.counter.AddCountersSourceEffect;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.abilities.mana.conditional.FilteredSpellManaCondition;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.counters.CounterType;
import mage.filter.FilterSpell;
import mage.filter.FilterTyped;
import mage.filter.common.FilterCreatureSpell;
import mage.filter.predicate.mageobject.ManaValuePredicate;
import mage.filter.predicate.typed.LogicalPredicate;
import mage.filter.predicate.typed.mageObject.cost.VariableManaCostPredicate;

import java.util.Set;
import java.util.UUID;

public class HelgaSkittishSeer extends CardImpl {

    private static final FilterSpell filter = new FilterCreatureSpell("a creature spell with mana value 4 or greater");
    private static final FilterTyped filterWithX = new FilterTyped("a creature spell with mana value 4 or greater or a creature spell with {X} in its mana cost")
            .addAll(
                    CardType.CREATURE.getPredicate(),
                    LogicalPredicate.or(
                            new mage.filter.predicate.typed.mageObject.value.ManaValuePredicate(ComparisonType.OR_GREATER, 4),
                            VariableManaCostPredicate.instance
                    )
            );

    static {
        filter.add(new ManaValuePredicate(ComparisonType.OR_GREATER, 4));
    }

    public HelgaSkittishSeer(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{G}{W}{U}");
        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.FROG);
        this.subtype.add(SubType.DRUID);
        this.power = new MageInt(1);
        this.toughness = new MageInt(3);

        // Whenever you cast a creature spell with mana value 4 or greater, you draw a card, gain 1 life and put a +1/+1 counter on Helga, Skittish Seer
        Ability ability = new SpellCastControllerTriggeredAbility(
                new DrawCardSourceControllerEffect(1, true), filter, false
        );
        ability.addEffect(new GainLifeEffect(1).setText(", gain 1 life"));
        ability.addEffect(new AddCountersSourceEffect(CounterType.P1P1.createInstance()).concatBy(", and"));

        this.addAbility(ability);

        // {T}: Add X mana of any one color, where X is Helga, Skittish Seer's power. Use this mana only to cast creature spells with mana value 4 or greater or to cas creature spells with {x} in their mana costs
        this.addAbility(new ComposedManaAbilityBuilder()
                .cost(new TapSourceCost())
                .addDynamicChoice(SourcePermanentPowerValue.NOT_NEGATIVE, Set.of(ManaType.WHITE, ManaType.BLUE, ManaType.BLACK, ManaType.RED, ManaType.GREEN))
                .condition(new FilteredSpellManaCondition(filterWithX))
                .ruleText("Add X mana of any one color, where X is {this}'s power. Spend this mana only to cast creature spells with mana value 4 or greater or creature spells with {X} in their mana costs")
                .build()
        );
    }

    private HelgaSkittishSeer(final HelgaSkittishSeer card) {
        super(card);
    }

    @Override
    public HelgaSkittishSeer copy() {
        return new HelgaSkittishSeer(this);
    }
}
