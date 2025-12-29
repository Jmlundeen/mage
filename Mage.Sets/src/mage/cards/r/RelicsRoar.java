package mage.cards.r;

import mage.abilities.effects.common.continuous.generic.ContinuousEffectBuilder;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.filter.StaticFilters;
import mage.target.TargetPermanent;

import java.util.UUID;

/**
 * @author TheElk801
 */
public final class RelicsRoar extends CardImpl {

    public RelicsRoar(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.INSTANT}, "{U}");

        // Until end of turn, target artifact or creature becomes a Dinosaur artifact creature with base power and toughness 4/3 in addition to its other types.
        this.getSpellAbility().addEffect(
                new ContinuousEffectBuilder(Duration.EndOfTurn, Outcome.BecomeCreature)
                        .withAddedCardTypes(CardType.CREATURE, CardType.ARTIFACT)
                        .withAddedSubTypes(false, SubType.DINOSAUR)
                        .withSetPowerAndToughness(4, 3)
        );
        this.getSpellAbility().addTarget(new TargetPermanent(StaticFilters.FILTER_PERMANENT_ARTIFACT_OR_CREATURE));
    }

    private RelicsRoar(final RelicsRoar card) {
        super(card);
    }

    @Override
    public RelicsRoar copy() {
        return new RelicsRoar(this);
    }
}
