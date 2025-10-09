package mage.cards.t;

import mage.abilities.Ability;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.effects.common.continuous.generic.ContinuousEffectBuilder;
import mage.abilities.effects.keyword.SurveilEffect;
import mage.abilities.keyword.EscapeAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.StaticFilters;

import java.util.UUID;

public class TheGrimCaptainsLocker extends CardImpl {

    public TheGrimCaptainsLocker(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT}, "{3}{B}");

        this.supertype.add(SuperType.LEGENDARY);

        // {T}, Surveil 1.
        this.addAbility(new SimpleActivatedAbility(new SurveilEffect(1), new TapSourceCost()));

        // {T}: Until end of turn, each creature card in your graveyard gains "Escape &mdash; {3}{B}, Exile four other cards from your graveyard."
        Ability ability = new SimpleActivatedAbility(new ContinuousEffectBuilder(Duration.EndOfTurn, Outcome.AddAbility)
                .setAffectedZones(Zone.GRAVEYARD)
                .setCardFilter(StaticFilters.FILTER_CARD_CREATURE)
                .withGainedAbility((card, source, game) -> new EscapeAbility(card, "{3}{B}" , 4))
                .setText("Until end of turn, each creature card in your graveyard gains " +
                        "\"Escape&mdash;{3}{B}, Exile four other cards from your graveyard.\""),
                new TapSourceCost());
        this.addAbility(ability);

    }

    private TheGrimCaptainsLocker(final TheGrimCaptainsLocker card) {
        super(card);
    }

    @Override
    public TheGrimCaptainsLocker copy() {
        return new TheGrimCaptainsLocker(this);
    }
}
