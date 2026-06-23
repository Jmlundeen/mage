package mage.cards.t;

import mage.abilities.Ability;
import mage.abilities.common.AttacksTriggeredAbility;
import mage.abilities.common.SpellCastControllerTriggeredAbility;
import mage.abilities.condition.Condition;
import mage.abilities.condition.common.SourceHasCounterCondition;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.decorator.ConditionalOneShotEffect;
import mage.abilities.dynamicvalue.DynamicValue;
import mage.abilities.dynamicvalue.common.CardsInControllerGraveyardCount;
import mage.abilities.effects.common.DamagePlayersEffect;
import mage.abilities.effects.common.TransformSourceEffect;
import mage.abilities.effects.common.counter.AddCountersSourceEffect;
import mage.abilities.hint.Hint;
import mage.abilities.hint.ValueHint;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.abilities.mana.conditional.FilteredSpellManaCondition;
import mage.cards.CardSetInfo;
import mage.cards.TransformingDoubleFacedCard;
import mage.constants.*;
import mage.counters.CounterType;
import mage.filter.FilterCard;
import mage.filter.StaticFilters;
import mage.filter.StaticTypedFilters;
import mage.filter.predicate.Predicates;

import java.util.Set;
import java.util.UUID;

/**
 * @author TheElk801
 */
public final class TheEmperorOfPalamecia extends TransformingDoubleFacedCard {

    private static final Condition condition = new SourceHasCounterCondition(CounterType.P1P1, 3);

    private static final FilterCard filter = new FilterCard("noncreature, nonland cards in your graveyard");

    static {
        filter.add(Predicates.not(CardType.CREATURE.getPredicate()));
        filter.add(Predicates.not(CardType.LAND.getPredicate()));
    }

    private static final DynamicValue xValue = new CardsInControllerGraveyardCount(filter);
    private static final Hint hint = new ValueHint("Noncreature, nonland cards in your graveyard", xValue);

    public TheEmperorOfPalamecia(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo,
                new SuperType[]{SuperType.LEGENDARY}, new CardType[]{CardType.CREATURE}, new SubType[]{SubType.HUMAN, SubType.NOBLE, SubType.WIZARD}, "{U}{R}",
                "The Lord Master of Hell",
                new SuperType[]{SuperType.LEGENDARY}, new CardType[]{CardType.CREATURE}, new SubType[]{SubType.DEMON, SubType.NOBLE, SubType.WIZARD}, "UR"
        );

        // The Emperor of Palamecia
        this.getLeftHalfCard().setPT(2, 2);

        // {T}: Add {U} or {R}. Spend this mana only to cast a noncreature spell.
        this.getLeftHalfCard().addAbility(
                ComposedManaAbilityBuilder.builder()
                        .cost(new TapSourceCost())
                        .addChoice(Set.of(ManaType.RED, ManaType.BLUE), 1)
                        .condition(new FilteredSpellManaCondition(StaticTypedFilters.A_NON_CREATURE_SPELL))
                        .ruleText("Add {U} or {R}. Spend this mana only to cast a noncreature spell")
                        .build()
        );

        // Whenever you cast a noncreature spell, if at least four mana was spent to cast it, put a +1/+1 counter on The Emperor of Palamecia. Then if it has three or more +1/+1 counters on it, transform it.
        Ability ability = new SpellCastControllerTriggeredAbility(
                new AddCountersSourceEffect(CounterType.P1P1.createInstance()),
                StaticFilters.FILTER_NONCREATURE_SPELL_FOUR_MANA_SPENT, false
        );
        ability.addEffect(new ConditionalOneShotEffect(
                new TransformSourceEffect(), condition,
                "Then if it has three or more +1/+1 counters on it, transform it"
        ));
        this.getLeftHalfCard().addAbility(ability);

        // The Lord Master of Hell
        this.getRightHalfCard().setPT(3, 3);

        // Starfall -- Whenever The Lord Master of Hell attacks, it deals X damage to each opponent, where X is the number of noncreature, nonland cards in your graveyard.
        this.getRightHalfCard().addAbility(new AttacksTriggeredAbility(new DamagePlayersEffect(
                xValue, TargetController.OPPONENT
        ).setText("it deals X damage to each opponent, where X is " +
                "the number of noncreature, nonland cards in your graveyard"))
                .withFlavorWord("Starfall").addHint(hint));
    }

    private TheEmperorOfPalamecia(final TheEmperorOfPalamecia card) {
        super(card);
    }

    @Override
    public TheEmperorOfPalamecia copy() {
        return new TheEmperorOfPalamecia(this);
    }
}
