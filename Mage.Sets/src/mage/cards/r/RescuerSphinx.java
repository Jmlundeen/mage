package mage.cards.r;

import mage.MageInt;
import mage.abilities.common.AsEntersBattlefieldAbility;
import mage.abilities.costs.common.ReturnToHandChosenControlledPermanentCost;
import mage.abilities.effects.common.DoIfCostPaid;
import mage.abilities.effects.common.continuous.replacement.EntersWithCountersEffect;
import mage.abilities.keyword.FlyingAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.ContinuousAffected;
import mage.constants.Duration;
import mage.constants.SubType;
import mage.counters.CounterType;
import mage.filter.StaticFilters;
import mage.target.common.TargetControlledPermanent;

import java.util.UUID;

/**
 * @author TheElk801
 */
public final class RescuerSphinx extends CardImpl {

    public RescuerSphinx(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{2}{U}{U}");

        this.subtype.add(SubType.SPHINX);
        this.power = new MageInt(3);
        this.toughness = new MageInt(2);

        // Flying
        this.addAbility(FlyingAbility.getInstance());

        // As Rescuer Sphinx enters the battlefield, you may return a nonland permanent you control to its owner's hand. If you do, Rescuer Sphinx enters the battlefield with a +1/+1 counter on it.
        this.addAbility(new AsEntersBattlefieldAbility(new DoIfCostPaid(
                new EntersWithCountersEffect(Duration.OneUse, ContinuousAffected.SOURCE, CounterType.P1P1.createInstance()),
                new ReturnToHandChosenControlledPermanentCost(new TargetControlledPermanent(StaticFilters.FILTER_CONTROLLED_PERMANENT_NON_LAND)),
                "Return a nonland permanent you control to its owner's hand for an additional counter?",
                true
        )));
    }

    private RescuerSphinx(final RescuerSphinx card) {
        super(card);
    }

    @Override
    public RescuerSphinx copy() {
        return new RescuerSphinx(this);
    }
}
