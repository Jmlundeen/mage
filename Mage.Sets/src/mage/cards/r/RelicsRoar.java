package mage.cards.r;

import mage.abilities.effects.common.continuous.BecomesCreatureTargetEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.StaticFilters;
import mage.game.permanent.token.custom.CreatureToken;
import mage.target.TargetPermanent;

import java.util.UUID;

/**
 * @author TheElk801
 */
public final class RelicsRoar extends CardImpl {

    public RelicsRoar(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.INSTANT}, "{U}");

        // Until end of turn, target artifact or creature becomes a Dinosaur artifact creature with base power and toughness 4/3 in addition to its other types.
        this.getSpellAbility().addEffect(new BecomesCreatureTargetEffect(
                new CreatureToken(4, 3, "Dinosaur artifact creature with base power and toughness 4/3 in addition to its other types"),
                false, false, Duration.EndOfTurn)
                .withDurationRuleAtStart(true)
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
