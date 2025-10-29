package mage.cards.n;

import mage.abilities.condition.common.SpellMasteryCondition;
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
import mage.target.common.TargetCardInGraveyard;

import java.util.UUID;

/**
 *
 * @author fireshoes
 */
public final class NecromanticSummons extends CardImpl {

    public NecromanticSummons(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.SORCERY}, "{4}{B}");

        this.getSpellAbility().addEffect(new ConditionalReplacementEffect(
                new EntersWithCountersEffect(Duration.EndOfTurn, ContinuousAffected.STATIC_OR_DYNAMIC, CounterType.P1P1.createInstance(2)),
                SpellMasteryCondition.instance).setText(" ")
        );// has to be added before the moving effect
        // Put target creature card from a graveyard onto the battlefield under your control.
        this.getSpellAbility().addEffect(new ReturnFromGraveyardToBattlefieldTargetEffect());
        this.getSpellAbility().addTarget(new TargetCardInGraveyard(StaticFilters.FILTER_CARD_CREATURE_A_GRAVEYARD));

        // <i>Spell mastery</i> &mdash; If there are two or more instant and/or sorcery cards in your graveyard, 
        // that creature enters the battlefield with two additional +1/+1 counters on it.
        this.getSpellAbility().addEffect(new InfoEffect("<br><i>Spell mastery</i> &mdash; If there are two or more "
                + "instant and/or sorcery cards in your graveyard, that creature enters with two additional +1/+1 counters on it"));
    }

    private NecromanticSummons(final NecromanticSummons card) {
        super(card);
    }

    @Override
    public NecromanticSummons copy() {
        return new NecromanticSummons(this);
    }
}
