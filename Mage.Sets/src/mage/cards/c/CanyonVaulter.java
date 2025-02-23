package mage.cards.c;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import mage.MageInt;
import mage.MageObjectReference;
import mage.abilities.Ability;
import mage.abilities.TriggeredAbilityImpl;
import mage.abilities.effects.Effect;
import mage.abilities.effects.common.continuous.GainAbilityTargetEffect;
import mage.abilities.keyword.FlyingAbility;
import mage.constants.*;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.target.targetpointer.FixedTarget;
import mage.watchers.Watcher;

/**
 *
 * @author Jmlundeen
 */
public final class CanyonVaulter extends CardImpl {

    public CanyonVaulter(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{1}{W}");
        
        this.subtype.add(SubType.KOR);
        this.subtype.add(SubType.PILOT);
        this.power = new MageInt(3);
        this.toughness = new MageInt(1);

        // Whenever this creature saddles a Mount or crews a Vehicle during your main phase, that Mount or Vehicle gains flying until end of turn.
        Ability ability = new CanyonVaulterTriggeredAbility(new GainAbilityTargetEffect(FlyingAbility.getInstance(), Duration.EndOfTurn)
                .setText("that Mount or Vehicle gains flying until end of turn"));
        this.addAbility(ability, new CanyonVaulterWatcher());

    }

    private CanyonVaulter(final CanyonVaulter card) {
        super(card);
    }

    @Override
    public CanyonVaulter copy() {
        return new CanyonVaulter(this);
    }
}

class CanyonVaulterWatcher extends Watcher {
    // watch for game event SADDLED_MOUNT and CREWED_VEHICLE and save MageObjectReference of the creature or vehicle
    // MOR -> saddled or crewed during main phase
    private final Map<MageObjectReference, MageObjectReference> crewedOrSaddledDuringMainPhase = new HashMap<>();

    CanyonVaulterWatcher() {
        super(WatcherScope.GAME);
    }

    @Override
    public void watch(GameEvent event, Game game) {
        if (event.getType() != GameEvent.EventType.SADDLED_MOUNT && event.getType() != GameEvent.EventType.CREWED_VEHICLE) {
            return;
        }
        if (game.getPhase().getType() != TurnPhase.PRECOMBAT_MAIN && game.getPhase().getType() != TurnPhase.POSTCOMBAT_MAIN) {
            return;
        }
        MageObjectReference vaulterMor = new MageObjectReference(event.getTargetId(), game);
        MageObjectReference mountVehicleMor = new MageObjectReference(event.getSourceId(), game);
        crewedOrSaddledDuringMainPhase.put(vaulterMor, mountVehicleMor);
    }

    public MageObjectReference getMountVehicleMor(MageObjectReference vaulterMor) {
        return crewedOrSaddledDuringMainPhase.get(vaulterMor);
    }

    @Override
    public void reset() {
        super.reset();
        crewedOrSaddledDuringMainPhase.clear();
    }
}

class CanyonVaulterTriggeredAbility extends TriggeredAbilityImpl {

    CanyonVaulterTriggeredAbility(Effect effect) {
        super(Zone.BATTLEFIELD, effect, false);
        setTriggerPhrase("Whenever {this} saddles a Mount or crews a Vehicle during your main phase, ");
    }

    private CanyonVaulterTriggeredAbility(final CanyonVaulterTriggeredAbility ability) {
        super(ability);
    }

    @Override
    public CanyonVaulterTriggeredAbility copy() {
        return new CanyonVaulterTriggeredAbility(this);
    }

    @Override
    public boolean checkEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.SADDLED_MOUNT || event.getType() == GameEvent.EventType.CREWED_VEHICLE;
    }

    @Override
    public boolean checkTrigger(GameEvent event, Game game) {
        CanyonVaulterWatcher watcher = game.getState().getWatcher(CanyonVaulterWatcher.class);
        if (watcher == null || !event.getTargetId().equals(this.getSourceId())) {
            return false;
        }
        MageObjectReference vaulterMor = new MageObjectReference(getSourceId(), game);
        MageObjectReference mountVehicleMor = watcher.getMountVehicleMor(vaulterMor);
        if (mountVehicleMor == null) {
            return false;
        }
        for (Effect effect : getEffects()) {
            effect.setTargetPointer(new FixedTarget(mountVehicleMor.getSourceId(), game));
        }
        return true;
    }
}
