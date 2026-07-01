package mage.target;

import mage.MageItem;
import mage.MageObject;
import mage.abilities.Ability;
import mage.constants.Zone;
import mage.filter.FilterTyped;
import mage.game.Game;
import mage.players.Player;
import mage.util.ObjectQuery;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class TargetGeneric extends TargetImpl {

    protected final FilterTyped filter;
    protected final Set<Zone> zones;

    public TargetGeneric(FilterTyped filter) {
        this(1, 1, filter, Set.of(Zone.ALL));
    }

    public TargetGeneric(int numTargets, FilterTyped filter) {
        this(numTargets, numTargets, filter, Set.of(Zone.ALL));
    }

    public TargetGeneric(FilterTyped filter, Zone... zones) {
        this(1, 1, filter, Set.of(zones));
    }

    public TargetGeneric(int numTargets, FilterTyped filter, Zone... zones) {
        this(numTargets, numTargets, filter, Set.of(zones));
    }

    public TargetGeneric(int minNumTargets, int maxNumTargets, FilterTyped filter, Zone... zones) {
        this(minNumTargets, maxNumTargets, filter, Set.of(zones));
    }

    public TargetGeneric(int minNumTargets, int maxNumTargets, FilterTyped filter, Set<Zone> zones) {
        this.minNumberOfTargets = minNumTargets;
        this.maxNumberOfTargets = maxNumTargets;
        this.filter = filter;
        this.targetName = filter.getMessage();
        this.zones = zones;
        if (this.zones != null && !this.zones.isEmpty()) {
            this.zone = this.zones.stream().findFirst().get();
        }
    }

    protected TargetGeneric(final TargetGeneric target) {
        super(target);
        this.filter = target.filter.copy();
        this.zones = target.zones;
    }

    @Override
    public String getTargetedName(Game game) {
        StringBuilder sb = new StringBuilder();
        for (UUID targetId : getTargets()) {
            Player player = game.getPlayer(targetId);
            if (player != null) {
                sb.append(player.getName()).append(" ");
                continue;
            }
            MageObject object = game.getObject(targetId);
            if (object != null) {
                sb.append(object.getName()).append(" ");
            }
        }
        return sb.toString().trim();
    }

    @Override
    public FilterTyped getFilter() {
        return filter;
    }

    public Set<Zone> getZones() {
        return zones;
    }

    @Override
    public TargetGeneric copy() {
        return new TargetGeneric(this);
    }

    @Override
    public TargetGeneric withNotTarget(boolean notTarget) {
        super.withNotTarget(notTarget);
        return this;
    }

    @Override
    public boolean canChoose(UUID sourceControllerId, Ability source, Game game) {
        return canChooseFromPossibleTargets(sourceControllerId, source, game);
    }

    @Override
    public Set<UUID> possibleTargets(UUID sourceControllerId, Ability source, Game game) {
        Set<UUID> possibleTargets = new HashSet<>();
        Player controller = game.getPlayer(sourceControllerId);
        if (controller == null) {
            return Set.of();
        }
        List<MageItem> results = ObjectQuery.query(game, controller, source, filter, zones);
        for (MageItem item : results) {
            possibleTargets.add(item.getId());
        }
        if (filter.canApplyToClass(Player.class)) {
            List<Player> players = ObjectQuery.queryPlayers(game, controller, source, filter);
            for (Player player : players) {
                possibleTargets.add(player.getId());
            }
        }
        return keepValidPossibleTargets(possibleTargets, sourceControllerId, source, game);
    }

    @Override
    public boolean canTarget(UUID id, Ability source, Game game) {
        if (filter.canApplyToClass(Player.class)) {
            Player player = game.getPlayer(id);
            if (player != null) {
                return filter.match(player, source.getControllerId(), source, game);
            }
        }
        MageObject object = game.getObject(id);
        if (object != null) {
            return filter.match(object, source.getControllerId(), source, game);
        }
        return false;
    }

    @Override
    public boolean canTarget(UUID playerId, UUID id, Ability source, Game game) {
        if (filter.canApplyToClass(Player.class)) {
            Player player = game.getPlayer(id);
            if (player != null) {
                return filter.match(player, playerId, source, game);
            }
        }
        MageObject object = game.getObject(id);
        if (object != null) {
            return filter.match(object, playerId, source, game);
        }
        return false;
    }
}
