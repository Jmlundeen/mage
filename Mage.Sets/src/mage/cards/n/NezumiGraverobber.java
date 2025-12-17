package mage.cards.n;

import mage.abilities.Ability;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.ExileTargetEffect;
import mage.abilities.effects.common.FlipSourceEffect;
import mage.abilities.effects.common.ReturnFromGraveyardToBattlefieldTargetEffect;
import mage.cards.Card;
import mage.cards.CardSetInfo;
import mage.cards.FlipCard;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.filter.FilterCard;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.players.Player;
import mage.target.Target;
import mage.target.common.TargetCardInGraveyard;
import mage.target.common.TargetCardInOpponentsGraveyard;

import java.util.UUID;

/**
 * @author Loki
 */
public final class NezumiGraverobber extends FlipCard {

    public NezumiGraverobber(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo,
                new CardType[]{CardType.CREATURE}, new SubType[]{SubType.RAT, SubType.ROGUE}, "{1}{B}",
                "Nighteyes the Desecrator",
                new SuperType[]{SuperType.LEGENDARY}, new CardType[]{CardType.CREATURE}, new SubType[]{SubType.RAT, SubType.WIZARD});

        // Nezumi Graverobber
        this.getLeftHalfCard().setPT(2, 1);

        // {1}{B}: Exile target card from an opponent's graveyard. If no cards are in that graveyard, flip Nezumi Graverobber.
        Ability ability = new SimpleActivatedAbility(new ExileTargetEffect(), new ManaCostsImpl<>("{1}{B}"));
        Target target = new TargetCardInOpponentsGraveyard(new FilterCard("card from an opponent's graveyard"));
        ability.addTarget(target);
        ability.addEffect(new NezumiGraverobberFlipEffect());
        this.getLeftHalfCard().addAbility(ability);

        // Nighteyes the Desecrator
        this.getRightHalfCard().setPT(4, 2);

        // {4}{B}: Put target creature card from a graveyard onto the battlefield under your control.
        Ability ability2 = new SimpleActivatedAbility(new ReturnFromGraveyardToBattlefieldTargetEffect(), new ManaCostsImpl<>("{4}{B}"));
        ability2.addTarget(new TargetCardInGraveyard(StaticFilters.FILTER_CARD_CREATURE_A_GRAVEYARD));
        this.getRightHalfCard().addAbility(ability2);
    }

    private NezumiGraverobber(final NezumiGraverobber card) {
        super(card);
    }

    @Override
    public NezumiGraverobber copy() {
        return new NezumiGraverobber(this);
    }
}

class NezumiGraverobberFlipEffect extends OneShotEffect {

    NezumiGraverobberFlipEffect() {
        super(Outcome.BecomeCreature);
        staticText = "If no cards are in that graveyard, flip {this}";
    }

    private NezumiGraverobberFlipEffect(final NezumiGraverobberFlipEffect effect) {
        super(effect);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Card card = game.getCard(getTargetPointer().getFirst(game, source));
        if (card != null) {
            Player player = game.getPlayer(card.getOwnerId());
            if (player != null) {
                if (player.getGraveyard().isEmpty()) {
                    return new FlipSourceEffect().apply(game, source);
                }
            }
        }
        return false;
    }

    @Override
    public NezumiGraverobberFlipEffect copy() {
        return new NezumiGraverobberFlipEffect(this);
    }

}
