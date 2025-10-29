package mage.cards.v;

import mage.abilities.condition.common.ManaWasSpentCondition;
import mage.abilities.decorator.ConditionalReplacementEffect;
import mage.abilities.effects.common.InfoEffect;
import mage.abilities.effects.common.ReturnFromGraveyardToBattlefieldTargetEffect;
import mage.abilities.effects.common.continuous.replacement.EntersWithCountersEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.ContinuousAffected;
import mage.constants.Duration;
import mage.counters.CounterType;
import mage.filter.StaticFilters;
import mage.target.common.TargetCardInYourGraveyard;

import java.util.UUID;

/**
 *
 * @author emerald000
 */
public final class VigorMortis extends CardImpl {

    public VigorMortis(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.SORCERY}, "{2}{B}{B}");

        // Return target creature card from your graveyard to the battlefield. If {G} was spent to cast Vigor Mortis, that creature enters the battlefield with an additional +1/+1 counter on it.
        this.getSpellAbility().addEffect(new ConditionalReplacementEffect(
                new EntersWithCountersEffect(Duration.EndOfTurn, ContinuousAffected.STATIC_OR_DYNAMIC, CounterType.P1P1.createInstance()),
                ManaWasSpentCondition.GREEN)
                .setText(" ")); // has to be added before the moving effect
        this.getSpellAbility().addEffect(new ReturnFromGraveyardToBattlefieldTargetEffect());
        this.getSpellAbility().addEffect(new InfoEffect("If {G} was spent to cast this spell, that creature enters with an additional +1/+1 counter on it"));
        this.getSpellAbility().addTarget(new TargetCardInYourGraveyard(StaticFilters.FILTER_CARD_CREATURE_YOUR_GRAVEYARD));
    }

    private VigorMortis(final VigorMortis card) {
        super(card);
    }

    @Override
    public VigorMortis copy() {
        return new VigorMortis(this);
    }
}
