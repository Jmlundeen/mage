package mage.constants;

import mage.cards.Card;
import mage.filter.predicate.ObjectSourcePlayer;
import mage.filter.predicate.ObjectSourcePlayerPredicate;
import mage.filter.predicate.TypedPredicate;
import mage.game.Controllable;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.players.Player;

import java.util.UUID;

/**
 * @author North
 */
public enum TargetController {

    ACTIVE,
    INACTIVE,
    ANY,
    YOU,
    NOT_YOU,
    OPPONENT,
    TEAM,
    OWNER,
    CONTROLLER_ATTACHED_TO,
    NEXT,
    EACH_PLAYER,
    ENCHANTED,
    SOURCE_TARGETS,
    MONARCH,
    SOURCE_CONTROLLER;

    private final OwnerPredicate ownerPredicate;
    private final PlayerPredicate playerPredicate;
    private final ControllerPredicate controllerPredicate;

    TargetController() {
        this.ownerPredicate = new OwnerPredicate(this);
        this.playerPredicate = new PlayerPredicate(this);
        this.controllerPredicate = new ControllerPredicate(this);
    }

    public OwnerPredicate getOwnerPredicate() {
        return ownerPredicate;
    }

    public PlayerPredicate getPlayerPredicate() {
        return playerPredicate;
    }

    public ControllerPredicate getControllerPredicate() {
        return controllerPredicate;
    }

    public static class OwnerPredicate implements ObjectSourcePlayerPredicate<Card>, TypedPredicate<ObjectSourcePlayer<Card>> {

        private final TargetController targetOwner;

        private OwnerPredicate(TargetController targetOwner) {
            this.targetOwner = targetOwner;
        }

        @Override
        public Class<Card> getObjectClass() {
            return Card.class;
        }

        @Override
        public boolean apply(ObjectSourcePlayer<Card> input, Game game) {
            Card card = input.getObject();
            UUID playerId = input.getPlayerId();
            if (card == null || playerId == null) {
                return false;
            }

            return switch (targetOwner) {
                case YOU -> card.isOwnedBy(playerId);
                case OPPONENT -> !card.isOwnedBy(playerId)
                        && game.getPlayer(playerId).hasOpponent(card.getOwnerId(), game);
                case NOT_YOU -> !card.isOwnedBy(playerId);
                case ENCHANTED -> {
                    Permanent permanent = input.getSource().getSourcePermanentIfItStillExists(game);
                    yield permanent != null && input.getObject().isOwnedBy(permanent.getAttachedTo());
                }
                case SOURCE_CONTROLLER -> card.isOwnedBy(input.getSource().getControllerId());
                case SOURCE_TARGETS -> card.isOwnedBy(input.getSource().getFirstTarget());
                case ACTIVE -> card.isOwnedBy(game.getActivePlayerId());
                case INACTIVE -> !card.isOwnedBy(game.getActivePlayerId());
                case MONARCH -> card.isOwnedBy(game.getMonarchId());
                case ANY -> true;
                default -> throw new UnsupportedOperationException("TargetController not supported");
            };
        }

        @Override
        public String toString() {
            return "Owner(" + targetOwner + ')';
        }
    }

    public static class PlayerPredicate implements ObjectSourcePlayerPredicate<Player>, TypedPredicate<ObjectSourcePlayer<Player>> {

        private final TargetController targetPlayer;

        private PlayerPredicate(TargetController player) {
            this.targetPlayer = player;
        }

        @Override
        public Class<Player> getObjectClass() {
            return Player.class;
        }

        @Override
        public boolean apply(ObjectSourcePlayer<Player> input, Game game) {
            Player player = input.getObject();
            UUID playerId = input.getPlayerId();
            if (player == null || playerId == null) {
                return false;
            }

            return switch (targetPlayer) {
                case YOU -> player.getId().equals(playerId);
                case OPPONENT -> !player.getId().equals(playerId) &&
                        game.getPlayer(playerId).hasOpponent(player.getId(), game);
                case NOT_YOU -> !player.getId().equals(playerId);
                case SOURCE_CONTROLLER -> player.getId().equals(input.getSource().getControllerId());
                case SOURCE_TARGETS -> player.getId().equals(input.getSource().getFirstTarget());
                case ACTIVE -> game.isActivePlayer(player.getId());
                case INACTIVE -> !game.isActivePlayer(player.getId());
                case MONARCH -> player.getId().equals(game.getMonarchId());
                default -> throw new UnsupportedOperationException("TargetController not supported");
            };
        }

        @Override
        public String toString() {
            return "Player(" + targetPlayer + ')';
        }
    }

    public static class ControllerPredicate implements ObjectSourcePlayerPredicate<Controllable>, TypedPredicate<ObjectSourcePlayer<Controllable>> {

        private final TargetController controller;

        private ControllerPredicate(TargetController controller) {
            this.controller = controller;
        }

        @Override
        public Class<Controllable> getObjectClass() {
            return Controllable.class;
        }

        @Override
        public boolean apply(ObjectSourcePlayer<Controllable> input, Game game) {
            Controllable object = input.getObject();
            UUID playerId = input.getPlayerId();

            return switch (controller) {
                case YOU -> object.isControlledBy(playerId);
                case TEAM -> !game.getPlayer(playerId).hasOpponent(object.getControllerId(), game);
                case OPPONENT -> !object.isControlledBy(playerId)
                        && game.getPlayer(playerId).hasOpponent(object.getControllerId(), game);
                case NOT_YOU -> !object.isControlledBy(playerId);
                case ACTIVE -> object.isControlledBy(game.getActivePlayerId());
                case INACTIVE -> !object.isControlledBy(game.getActivePlayerId());
                case ENCHANTED -> {
                    Permanent permanent = input.getSource().getSourcePermanentIfItStillExists(game);
                    yield permanent != null && input.getObject().isControlledBy(permanent.getAttachedTo());
                }
                case SOURCE_CONTROLLER -> object.isControlledBy(input.getSource().getControllerId());
                case SOURCE_TARGETS -> object.isControlledBy(input.getSource().getFirstTarget());
                case MONARCH -> object.isControlledBy(game.getMonarchId());
                case ANY -> true;
                default -> throw new UnsupportedOperationException("TargetController not supported");
            };
        }

        @Override
        public String toString() {
            return "TargetController (" + controller.toString() + ')';
        }

        /**
         * For tests
         */
        public TargetController getController() {
            return this.controller;
        }
    }
}
