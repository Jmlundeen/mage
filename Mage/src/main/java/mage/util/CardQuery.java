package mage.util;

import mage.MageObject;
import mage.abilities.Ability;
import mage.cards.Card;
import mage.constants.TargetController;
import mage.constants.Zone;
import mage.filter.FilterCard;
import mage.filter.FilterPermanent;
import mage.filter.FilterStackObject;
import mage.game.Game;
import mage.game.command.Commander;
import mage.game.permanent.Permanent;
import mage.game.stack.Spell;
import mage.players.Player;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class CardQuery {

    private final TargetController targetController;
    private final FilterCard cardFilter;
    private final FilterStackObject stackObjectFilter;
    private final FilterPermanent permanentFilter;

    public CardQuery(TargetController targetController, FilterCard cardFilter) {
        this(targetController, cardFilter, null, null);
    }

    public CardQuery(TargetController targetController, FilterStackObject stackObjectFilter) {
        this(targetController, null, stackObjectFilter, null);
    }

    public CardQuery(TargetController targetController, FilterPermanent permanentFilter) {
        this(targetController, null, null, permanentFilter);
    }

    public CardQuery(TargetController targetController, FilterCard cardFilter, FilterStackObject stackObjectFilter, FilterPermanent permanentFilter) {
        this.targetController = targetController;
        this.cardFilter = cardFilter;
        this.stackObjectFilter = stackObjectFilter;
        this.permanentFilter = permanentFilter;
    }

    public void getObjectsFromZone(Game game, Zone zone, Player controller, Ability source, List<MageObject> affectedObjects) {
        if (this.targetController.equals(TargetController.YOU)) {
            getPlayersObjectsFromZone(game, zone, controller, source, affectedObjects);
            return;
        }

        for (UUID playerId : game.getOpponents(controller.getId(), true)) {
            Player opponent = game.getPlayer(playerId);
            if (opponent == null) {
                continue;
            }
            getPlayersObjectsFromZone(game, zone, opponent, source, affectedObjects);
        }
        if (this.targetController.equals(TargetController.EACH_PLAYER)) {
            getPlayersObjectsFromZone(game, zone, controller, source, affectedObjects);
        }
    }

    protected void getPlayersObjectsFromZone(Game game, Zone zone, Player player, Ability source, List<MageObject> affectedObjects) {
        switch (zone) {
            case GRAVEYARD:
                affectedObjects.addAll(player.getGraveyard().getCards(game).stream()
                        .filter(card -> cardFilter == null || cardFilter.match(card, player.getId(), source, game))
                        .collect(Collectors.toList()));
                break;
            case HAND:
                affectedObjects.addAll(player.getHand().getCards(game).stream()
                        .filter(card -> cardFilter == null || cardFilter.match(card, player.getId(), source, game))
                        .collect(Collectors.toList()));
                break;
            case LIBRARY:
                affectedObjects.addAll(player.getLibrary().getCards(game).stream()
                        .filter(card -> cardFilter == null || cardFilter.match(card, player.getId(), source, game))
                        .collect(Collectors.toList()));
                break;
            case EXILED:
                affectedObjects.addAll(game.getExile().getCardsOwned(cardFilter, player.getId(), source, game));
                break;
            case COMMAND:
                for (Object commObj : game.getState().getCommand()) {
                    if (commObj instanceof Commander) {
                        Card card = game.getCard(((Commander) commObj).getId());
                        if (card != null && card.getControllerOrOwnerId().equals(player.getId()) &&
                                (cardFilter == null || cardFilter.match(card, player.getId(), source, game))) {
                            affectedObjects.add(card);
                        }
                    }
                }
                break;
            case STACK:
                affectedObjects.addAll(game.getStack().stream()
                        .filter(stackObject -> stackObject.getControllerId().equals(player.getId()))
                        .filter(stackObject -> stackObjectFilter != null ? stackObjectFilter.match(stackObject, player.getId(), source, game)
                                : cardFilter == null || (stackObject instanceof Spell && cardFilter.match(((Spell) stackObject), player.getId(), source, game)))
                        .map(stackObject -> game.getCard(stackObject.getSourceId()))
                        .collect(Collectors.toList())
                );
                break;
            case BATTLEFIELD:
                if (permanentFilter == null) {
                    throw new IllegalArgumentException("Permanent filter must be defined for battlefield zone");
                }
                for (Permanent permanent : game.getBattlefield().getAllActivePermanents(player.getId())) {
                    if (permanentFilter.match(permanent, player.getId(), source, game)) {
                        affectedObjects.add(permanent);
                    }
                }
                break;
        }
    }
}
