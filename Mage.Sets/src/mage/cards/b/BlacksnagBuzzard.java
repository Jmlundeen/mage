package mage.cards.b;

import mage.MageInt;
import mage.abilities.common.EntersBattlefieldAbility;
import mage.abilities.condition.common.MorbidCondition;
import mage.abilities.effects.common.continuous.replacement.EntersWithCountersEffect;
import mage.abilities.hint.common.MorbidHint;
import mage.abilities.keyword.FlyingAbility;
import mage.abilities.keyword.PlotAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.counters.CounterType;

import java.util.UUID;

/**
 * @author TheElk801
 */
public final class BlacksnagBuzzard extends CardImpl {

    public BlacksnagBuzzard(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{2}{B}");

        this.subtype.add(SubType.BIRD);
        this.power = new MageInt(2);
        this.toughness = new MageInt(1);

        // Flying
        this.addAbility(FlyingAbility.getInstance());

        // Blacksnag Buzzard enters the battlefield with a +1/+1 counter on it if a creature died this turn.
        this.addAbility(new EntersBattlefieldAbility(new EntersWithCountersEffect(CounterType.P1P1.createInstance()),
                MorbidCondition.instance,
                "{this} enters with a +1/+1 counter on it if a creature died this turn.").addHint(MorbidHint.instance));

        // Plot {1}{B}
        this.addAbility(new PlotAbility("{1}{B}"));
    }

    private BlacksnagBuzzard(final BlacksnagBuzzard card) {
        super(card);
    }

    @Override
    public BlacksnagBuzzard copy() {
        return new BlacksnagBuzzard(this);
    }
}
