package mage.view;

import mage.game.Game;
import mage.game.combat.CombatGroup;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.ws.view.ViewProto;

import java.io.Serializable;
import java.util.UUID;

/**
 *
 * @author BetaSteward_at_googlemail.com
 */
public class CombatGroupView implements Serializable {
    private static final long serialVersionUID = 1L;

    private final CardsView attackers = new CardsView();
    private final CardsView blockers = new CardsView();
    private final boolean isBlocked;
    private String defenderName = "";
    private final UUID defenderId;

    public CombatGroupView(CombatGroup combatGroup, Game game) {
        Player player = game.getPlayer(combatGroup.getDefenderId());
        if (player != null) {
            this.defenderName = player.getName();
        }
        else {
            Permanent perm = game.getPermanent(combatGroup.getDefenderId());
            if (perm != null) {
                this.defenderName = perm.getName();
            }
        }
        this.defenderId = combatGroup.getDefenderId();
        for (UUID id: combatGroup.getAttackers()) {
            Permanent attacker = game.getPermanent(id);
            if (attacker != null) {
                attackers.put(id, new PermanentView(attacker, game.getCard(attacker.getId()),null, game));
            }
        }
        for (UUID id: combatGroup.getBlockers()) {
            Permanent blocker = game.getPermanent(id);
            if (blocker != null) {
                blockers.put(id, new PermanentView(blocker, game.getCard(blocker.getId()), null, game));
            }
        }
        isBlocked = combatGroup.getBlocked();
    }

    // private constructor for fromProto
    private CombatGroupView(ViewProto.CombatGroupView proto) {
        this.isBlocked = proto.getIsBlocked();
        this.defenderName = proto.getDefenderName();
        this.defenderId = proto.getDefenderId().isEmpty() ? null : UUID.fromString(proto.getDefenderId());
        proto.getAttackersMap().forEach((uuid, cardProto) -> attackers.put(UUID.fromString(uuid), PermanentView.fromProto(cardProto)));
        proto.getBlockersMap().forEach((uuid, cardProto) -> blockers.put(UUID.fromString(uuid), PermanentView.fromProto(cardProto)));
    }

    public String getDefenderName() {
        return defenderName;
    }

    public CardsView getAttackers() {
        return attackers;
    }

    public CardsView getBlockers() {
        return blockers;
    }

    public UUID getDefenderId() {
        return defenderId;
    }

    public boolean isBlocked() {
        return isBlocked;
    }

    public ViewProto.CombatGroupView toProto() {
        ViewProto.CombatGroupView.Builder builder = ViewProto.CombatGroupView.newBuilder()
                .setIsBlocked(isBlocked)
                .setDefenderName(defenderName != null ? defenderName : "")
                .setDefenderId(defenderId != null ? defenderId.toString() : "");

        attackers.forEach((uuid, cardView) -> builder.putAttackers(uuid.toString(), ((PermanentView) cardView).toPermanentViewProto()));
        blockers.forEach((uuid, cardView) -> builder.putBlockers(uuid.toString(), ((PermanentView) cardView).toPermanentViewProto()));

        return builder.build();
    }

    public static CombatGroupView fromProto(ViewProto.CombatGroupView proto) {
        return new CombatGroupView(proto);
    }
}
