package mage.cards.s;

import mage.abilities.condition.common.MorbidCondition;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.dynamicvalue.common.CountersSourceCount;
import mage.abilities.effects.common.counter.AddCountersSourceEffect;
import mage.abilities.hint.common.MorbidHint;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.abilities.mana.conditional.FilteredSpellManaCondition;
import mage.abilities.triggers.BeginningOfEndStepTriggeredAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.AbilityWord;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.constants.TargetController;
import mage.counters.CounterType;
import mage.filter.FilterTyped;
import mage.filter.predicate.typed.LogicalPredicate;

import java.util.UUID;

/**
 * @author Cguy7777
 */
public final class SeanceBoard extends CardImpl {

    private static final FilterTyped filter = new FilterTyped("instant, sorcery, Demon, and Spirit spells")
            .add(LogicalPredicate.or(
                    CardType.INSTANT.getPredicate(),
                    CardType.SORCERY.getPredicate(),
                    SubType.DEMON.getPredicate(),
                    SubType.SPIRIT.getPredicate()
            ));

    public SeanceBoard(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT}, "{2}");

        // Morbid -- At the beginning of each end step, if a creature died this turn, put a soul counter on Seance Board.
        this.addAbility(new BeginningOfEndStepTriggeredAbility(
                TargetController.ANY, new AddCountersSourceEffect(CounterType.SOUL.createInstance()),
                false, MorbidCondition.instance
        ).addHint(MorbidHint.instance).setAbilityWord(AbilityWord.MORBID));

        // {T}: Add X mana of any one color, where X is the number of soul counters on Seance Board.
        // Spend this mana only to cast instant, sorcery, Demon, and Spirit spells.
        this.addAbility(new ComposedManaAbilityBuilder()
                .cost(new TapSourceCost())
                .addChoiceAnyOneColor(new CountersSourceCount(CounterType.SOUL))
                .condition(new FilteredSpellManaCondition(filter))
                .ruleText("Add X mana of any one color, where X is the number of soul counters on {this}. Spend this mana only to cast instant, sorcery, Demon, and Spirit spells")
                .build()
        );
    }

    private SeanceBoard(final SeanceBoard card) {
        super(card);
    }

    @Override
    public SeanceBoard copy() {
        return new SeanceBoard(this);
    }
}
