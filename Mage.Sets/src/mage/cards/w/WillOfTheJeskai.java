package mage.cards.w;

import mage.MageItem;
import mage.MageObjectReference;
import mage.abilities.Ability;
import mage.abilities.Mode;
import mage.abilities.condition.common.ControlACommanderCondition;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.continuous.generic.ContinuousEffectBuilder;
import mage.abilities.keyword.FlashbackAbility;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.players.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 *
 * @author Jmlundeen
 */
public final class WillOfTheJeskai extends CardImpl {

    public WillOfTheJeskai(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.SORCERY}, "{3}{R}");
        

        // Choose one. If you control a commander as you cast this spell, you may choose both instead.
        this.getSpellAbility().getModes().setChooseText(
                "Choose one. If you control a commander as you cast this spell, you may choose both instead."
        );
        this.getSpellAbility().getModes().setMoreCondition(2, ControlACommanderCondition.instance);

        // * Each player may discard their hand and draw five cards.
        this.getSpellAbility().addEffect(new WillOfTheJeskaiEffect());

        // * Each instant and sorcery card in your graveyard gains flashback until end of turn. The flashback cost is equal to its mana cost.
        Mode mode = new Mode(new ContinuousEffectBuilder(Duration.EndOfTurn, Outcome.AddAbility)
                .setAffectedZones(Zone.GRAVEYARD)
                .setCardFilter(StaticFilters.FILTER_CARD_INSTANT_OR_SORCERY)
                .withGainedAbility((card, source, game) -> new FlashbackAbility(card, card.getManaCost()))
                .setText("Each instant and sorcery card in your graveyard gains flashback until end of turn. " +
                        "The flashback cost is equal to its mana cost"));
        this.getSpellAbility().addMode(mode);
    }

    private WillOfTheJeskai(final WillOfTheJeskai card) {
        super(card);
    }

    @Override
    public WillOfTheJeskai copy() {
        return new WillOfTheJeskai(this);
    }
}

class WillOfTheJeskaiEffect extends OneShotEffect {

    WillOfTheJeskaiEffect() {
        super(Outcome.Benefit);
        staticText = "each player may discard their hand and draw five cards";
    }

    private WillOfTheJeskaiEffect(final WillOfTheJeskaiEffect effect) {
        super(effect);
    }

    @Override
    public WillOfTheJeskaiEffect copy() {
        return new WillOfTheJeskaiEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        List<Player> wheelers = new ArrayList<>();
        for (UUID playerId : game.getState().getPlayersInRange(source.getControllerId(), game)) {
            Player player = game.getPlayer(playerId);
            if (player != null && player.chooseUse(
                    Outcome.DrawCard, "Discard your hand and draw five?", source, game
            )) {
                game.informPlayers(player.getName() + " chooses to discard their hand and draw five");
                wheelers.add(player);
            }
        }
        for (Player player : wheelers) {
            player.discard(player.getHand(), false, source, game);
            player.drawCards(5, source, game);
        }
        return true;
    }
}
