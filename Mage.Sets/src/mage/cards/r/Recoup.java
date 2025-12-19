package mage.cards.r;

import mage.MageItem;
import mage.abilities.Ability;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.effects.common.continuous.generic.ContinuousEffectBuilder;
import mage.abilities.keyword.FlashbackAbility;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.FilterCard;
import mage.game.Game;
import mage.players.Player;
import mage.target.common.TargetCardInYourGraveyard;

import java.util.List;
import java.util.UUID;

/**
 * @author cbt33, BetaSteward (PastInFlames)
 */
public final class Recoup extends CardImpl {

    private static final FilterCard filter = new FilterCard("sorcery card");

    static {
        filter.add(CardType.SORCERY.getPredicate());
    }

    public Recoup(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.SORCERY}, "{1}{R}");

        // Target sorcery card in your graveyard gains flashback until end of turn. The flashback cost is equal to its mana cost.
        this.getSpellAbility().addEffect(new ContinuousEffectBuilder(Duration.EndOfTurn, Outcome.AddAbility)
                .withGainedAbility((card, source, game) -> new FlashbackAbility(card, card.getManaCost()))
                .setText("Target sorcery card in your graveyard gains flashback until end of turn. The flashback cost is equal to its mana cost")
                );
        this.getSpellAbility().addTarget(new TargetCardInYourGraveyard(filter));

        // Flashback {3}{R}
        this.addAbility(new FlashbackAbility(this, new ManaCostsImpl<>("{3}{R}")));
    }

    private Recoup(final Recoup card) {
        super(card);
    }

    @Override
    public Recoup copy() {
        return new Recoup(this);
    }
}
