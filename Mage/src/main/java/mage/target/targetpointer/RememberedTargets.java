package mage.target.targetpointer;

import mage.MageItem;
import mage.MageObject;
import mage.MageObjectReference;
import mage.abilities.Ability;
import mage.cards.Cards;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.players.Player;

import java.util.*;

public class RememberedTargets extends TargetPointerImpl {

    private final LinkedHashMap<UUID, MageObjectReference> targets = new LinkedHashMap<>();

    public <T extends MageItem> RememberedTargets(T object, Game game) {
        addTarget(object, game);
        this.setInitialized();
    }

    public <T extends MageItem> RememberedTargets(List<T> objects, Game game) {
        objects.forEach(object -> addTarget(object, game));
        this.setInitialized();
    }

    public RememberedTargets(Cards cards, Game game) {
        cards.getCards(game).forEach(card -> addTarget(card, game));
        this.setInitialized();
    }

    protected RememberedTargets(final RememberedTargets targetPointer) {
        super(targetPointer);
        this.targets.putAll(targetPointer.targets);
    }

    @Override
    public void init(Game game, Ability source) {
    }

    @Override
    public List<UUID> getTargets(Game game, Ability source) {
        return targets.entrySet().stream()
                .filter(entry -> entry.getValue() == null || entry.getValue().zoneCounterIsCurrent(game))
                .map(Map.Entry::getKey)
                .toList();
    }

    @Override
    public UUID getFirst(Game game, Ability source) {
        return targets.entrySet().stream()
                .filter(entry -> entry.getValue() == null || entry.getValue().zoneCounterIsCurrent(game))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }

    @Override
    public RememberedTargets copy() {
        return new RememberedTargets(this);
    }

    @Override
    public Permanent getFirstTargetPermanentOrLKI(Game game, Ability source) {
        return targets.values().stream()
                .filter(Objects::nonNull)
                .map(mor -> mor.getPermanentOrLKIBattlefield(game))
                .findFirst()
                .orElse(null);
    }

    private <T extends MageItem> void addTarget(T object, Game game) {
        if (object instanceof Player) {
            targets.put(object.getId(), null);
        } else {
            targets.put(object.getId(), new MageObjectReference((MageObject) object, game));
        }
    }
}
