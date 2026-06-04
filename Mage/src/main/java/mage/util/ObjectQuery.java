package mage.util;

import mage.MageItem;
import mage.abilities.Ability;
import mage.cards.Card;
import mage.constants.Zone;
import mage.filter.FilterTyped;
import mage.game.Game;
import mage.game.command.Commander;
import mage.game.permanent.Permanent;
import mage.players.Player;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class ObjectQuery {

    private ObjectQuery() {
    }

    public static List<Card> queryCards(Game game, Player controller, Ability source, Set<Zone> zones, FilterTyped filter) {
        List<MageItem> affectedObjects = getAffectedObjects(game, controller, source, zones, filter);
        return affectedObjects.stream()
                .filter(Card.class::isInstance)
                .map(Card.class::cast)
                .toList();
    }

    public static List<Permanent> queryPermanents(Game game, Player controller, Ability source, FilterTyped filter) {
        List<MageItem> affectedObjects = new ArrayList<>();
        getObjectsFromZone(game, Zone.BATTLEFIELD, controller, source, affectedObjects, filter);
        return affectedObjects.stream()
                .filter(Permanent.class::isInstance)
                .map(Permanent.class::cast)
                .toList();
    }

    public static List<MageItem> query(Game game, Player controller, Ability source, FilterTyped filter, Set<Zone> zones) {
        return getAffectedObjects(game, controller, source, zones, filter);
    }

    public static List<Player> queryPlayers(Game game, Player controller, Ability source, FilterTyped filter) {
        List<Player> affectedPlayers = new ArrayList<>();
        for (UUID playerId : game.getState().getPlayersInRange(controller.getId(), game)) {
            Player targetPlayer = game.getPlayer(playerId);
            if (targetPlayer != null && filter.match(targetPlayer, controller.getId(), source, game)) {
                affectedPlayers.add(targetPlayer);
            }
        }
        return affectedPlayers;
    }

    public static List<Ability> queryAbilities(Game game, Player controller, Ability source, Set<Zone> zones, FilterTyped filter) {
        List<MageItem> affectedObjects = getAffectedObjects(game, controller, source, zones, filter);
        List<Ability> abilities = new ArrayList<>();
        affectedObjects.stream()
                .filter(Ability.class::isInstance)
                .map(Ability.class::cast)
                .forEach(abilities::add);
        affectedObjects.stream()
                .filter(Card.class::isInstance)
                .map(Card.class::cast)
                .flatMap(card -> card.getAbilities(game).stream())
                .filter(ability -> filter.match(ability, controller.getId(), source, game))
                .forEach(abilities::add);
        return abilities;
    }

    private static @NonNull List<MageItem> getAffectedObjects(Game game, Player controller, Ability source, Set<Zone> zones, FilterTyped filter) {
        List<MageItem> affectedObjects = new ArrayList<>();
        if (zones.size() == 1 && zones.contains(Zone.ALL)) {
            for (Zone zone : Zone.values()) {
                getObjectsFromZone(game, zone, controller, source, affectedObjects, filter);
            }
            return affectedObjects;
        }
        for (Zone zone : zones) {
            getObjectsFromZone(game, zone, controller, source, affectedObjects, filter);
        }
        return affectedObjects;
    }

    public static void getObjectsFromZone(Game game, Zone zone, Player controller, Ability source, List<MageItem> affectedObjects, FilterTyped filter) {
        if (game == null || controller == null || filter == null) {
            return;
        }

        for (UUID playerId : game.getState().getPlayersInRange(controller.getId(), game)) {
            Player targetPlayer = game.getPlayer(playerId);
            if (targetPlayer == null) {
                continue;
            }
            getPlayersObjectsFromZone(game, zone, controller, targetPlayer, source, affectedObjects, filter);
        }
    }

    protected static void getPlayersObjectsFromZone(Game game, Zone zone, Player sourcePlayer, Player targetPlayer, Ability source, List<MageItem> affectedObjects, FilterTyped filter) {
        switch (zone) {
            case GRAVEYARD:
                affectedObjects.addAll(targetPlayer.getGraveyard().getCards(game).stream()
                        .filter(card -> filter.match(card, sourcePlayer.getId(), source, game))
                        .toList());
                break;
            case HAND:
                affectedObjects.addAll(targetPlayer.getHand().getCards(game).stream()
                        .filter(card -> filter.match(card, sourcePlayer.getId(), source, game))
                        .toList());
                break;
            case LIBRARY:
                affectedObjects.addAll(targetPlayer.getLibrary().getCards(game).stream()
                        .filter(card -> filter.match(card, sourcePlayer.getId(), source, game))
                        .toList());
                break;
            case EXILED:
                affectedObjects.addAll(game.getExile().getCardsOwned(filter, targetPlayer.getId(), source, game));
                break;
            case COMMAND:
                for (Object commObj : game.getState().getCommand()) {
                    if (commObj instanceof Commander) {
                        Card card = game.getCard(((Commander) commObj).getId());
                        if (card != null && card.getControllerOrOwnerId().equals(sourcePlayer.getId()) &&
                                (filter.match(card, sourcePlayer.getId(), source, game))) {
                            affectedObjects.add(card);
                        }
                    }
                }
                break;
            case STACK:
                affectedObjects.addAll(game.getStack().stream()
                        .filter(stackObject -> stackObject.getControllerId().equals(targetPlayer.getId()))
                        .filter(stackObject -> filter.match(stackObject, sourcePlayer.getId(), source, game))
                        .toList()
                );
                break;
            case BATTLEFIELD:
                for (Permanent permanent : game.getBattlefield().getAllActivePermanents(targetPlayer.getId())) {
                    if (filter.match(permanent, sourcePlayer.getId(), source, game)) {
                        affectedObjects.add(permanent);
                    }
                }
                break;
        }
    }
}
