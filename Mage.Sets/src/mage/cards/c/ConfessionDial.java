package mage.cards.c;

import mage.abilities.Ability;
import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.effects.common.continuous.generic.ContinuousEffectBuilder;
import mage.abilities.effects.keyword.SurveilEffect;
import mage.abilities.keyword.EscapeAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.common.FilterCreatureCard;
import mage.target.common.TargetCardInYourGraveyard;

import java.util.UUID;

/**
 * @author Cguy7777
 */
public final class ConfessionDial extends CardImpl {

    private static final FilterCreatureCard filter
            = new FilterCreatureCard("legendary creature card in your graveyard");

    static {
        filter.add(SuperType.LEGENDARY.getPredicate());
    }

    public ConfessionDial(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT}, "{3}");

        // When Confession Dial enters the battlefield, surveil 3.
        this.addAbility(new EntersBattlefieldTriggeredAbility(new SurveilEffect(3, false)));

        // {T}: Target legendary creature card in your graveyard gains escape until end of turn.
        // The escape cost is equal to its mana cost plus exile three other cards from your graveyard.
        Ability ability = new SimpleActivatedAbility(new ContinuousEffectBuilder(Duration.EndOfTurn, Outcome.AddAbility)
                .withGainedAbility((card, source, game) -> new EscapeAbility(card, card.getManaCost().getText(), 3))
                .setText("Target legendary creature card in your graveyard gains escape until end of turn. " +
                        "The escape cost is equal to its mana cost plus exile three other cards from your graveyard. " +
                        "<i>(You may cast it from your graveyard for its escape cost this turn.)</i>"), new TapSourceCost());
        ability.addTarget(new TargetCardInYourGraveyard(filter));
        this.addAbility(ability);
    }

    private ConfessionDial(final ConfessionDial card) {
        super(card);
    }

    @Override
    public ConfessionDial copy() {
        return new ConfessionDial(this);
    }
}
