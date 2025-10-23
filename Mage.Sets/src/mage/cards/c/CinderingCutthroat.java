package mage.cards.c;

import mage.MageInt;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.condition.common.OpponentsLostLifeCondition;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.decorator.ConditionalReplacementEffect;
import mage.abilities.effects.common.continuous.generic.ContinuousEffectBuilder;
import mage.abilities.effects.common.continuous.replacement.EntersWithCountersEffect;
import mage.abilities.hint.common.OpponentsLostLifeHint;
import mage.abilities.keyword.MenaceAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.counters.CounterType;

import java.util.UUID;

/**
 * @author Susucr
 */
public final class CinderingCutthroat extends CardImpl {

    public CinderingCutthroat(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{2}{B/R}");

        this.subtype.add(SubType.LIZARD);
        this.subtype.add(SubType.ASSASSIN);
        this.power = new MageInt(3);
        this.toughness = new MageInt(2);

        // Cindering Cutthroat enters with a +1/+1 counter on it if an opponent lost life this turn.
        this.addAbility(new SimpleStaticAbility(new ConditionalReplacementEffect(
                        new EntersWithCountersEffect(CounterType.P1P1.createInstance()),
                        OpponentsLostLifeCondition.instance)
                .setText("{this} enters with a +1/+1 counter on it if an opponent lost life this turn")
        ).addHint(OpponentsLostLifeHint.instance));

        // {1}{B/R}: Cindering Cutthroat gains menace until end of turn.
        this.addAbility(new SimpleActivatedAbility(
                new ContinuousEffectBuilder(Duration.EndOfTurn, Outcome.AddAbility, ContinuousAffected.SOURCE)
                        .withGainedAbilities(new MenaceAbility())
                        .setText("{this} gains menace until end of turn"),
                new ManaCostsImpl<>("{1}{B/R}")
        ));
    }

    private CinderingCutthroat(final CinderingCutthroat card) {
        super(card);
    }

    @Override
    public CinderingCutthroat copy() {
        return new CinderingCutthroat(this);
    }
}
