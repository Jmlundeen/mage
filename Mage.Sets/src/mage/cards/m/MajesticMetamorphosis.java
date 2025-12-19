package mage.cards.m;

import mage.abilities.effects.common.DrawCardSourceControllerEffect;
import mage.abilities.effects.common.continuous.BecomesCreatureTargetEffect;
import mage.abilities.keyword.FlyingAbility;
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
public final class MajesticMetamorphosis extends CardImpl {

    public MajesticMetamorphosis(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.INSTANT}, "{2}{U}");

        // Until end of turn, target artifact or creature becomes a 4/4 Angel artifact creature and gains flying.
        this.getSpellAbility().addEffect(new BecomesCreatureTargetEffect(
                new CreatureToken(4, 4, "4/4 Angel artifact creature and gains flying")
                        .withType(CardType.ARTIFACT)
                        .withSubType(SubType.ANGEL)
                        .withAbility(FlyingAbility.getInstance()),
                false, false, Duration.EndOfTurn)
                .withDurationRuleAtStart(true)
        );
        this.getSpellAbility().addTarget(new TargetPermanent(StaticFilters.FILTER_PERMANENT_ARTIFACT_OR_CREATURE));

        // Draw a card.
        this.getSpellAbility().addEffect(new DrawCardSourceControllerEffect(1).concatBy("<br>"));
    }

    private MajesticMetamorphosis(final MajesticMetamorphosis card) {
        super(card);
    }

    @Override
    public MajesticMetamorphosis copy() {
        return new MajesticMetamorphosis(this);
    }
}
