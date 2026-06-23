package mage.abilities.mana;


import mage.abilities.DelayedTriggeredAbility;
import mage.abilities.effects.Effect;
import mage.constants.Duration;
import mage.constants.SetTargetPointer;
import mage.filter.FilterTyped;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.game.events.TappedForManaEvent;
import mage.game.permanent.Permanent;
import mage.target.targetpointer.FixedTarget;

/**
 * @author Plopman
 */
public class DelayedTriggeredManaAbility extends DelayedTriggeredAbility {

    private final FilterTyped filter;
    private GameEvent.EventType eventType = GameEvent.EventType.TAPPED_FOR_MANA;
    private SetTargetPointer setTargetPointer = SetTargetPointer.NONE;

    protected DelayedTriggeredManaAbility(final DelayedTriggeredManaAbility ability) {
        super(ability);
        this.filter = ability.filter.copy();
        this.eventType = ability.eventType;
        this.setTargetPointer = ability.setTargetPointer;
    }

    public DelayedTriggeredManaAbility(String triggerPhrase, FilterTyped filter, Effect effect, Duration duration, Boolean triggerOnlyOnce) {
        super(effect, duration, triggerOnlyOnce);
        this.filter = filter;
        this.usesStack = false;
        setTriggerPhrase(triggerPhrase);
    }

    @Override
    public boolean checkEventType(GameEvent event, Game game) {
        return event.getType() == eventType;
    }

    @Override
    public boolean checkTrigger(GameEvent event, Game game) {
        Permanent permanent;
        if (event instanceof TappedForManaEvent) {
            permanent = ((TappedForManaEvent) event).getPermanent();
        } else {
            permanent = game.getPermanentOrLKIBattlefield(event.getSourceId());
        }
        if (permanent == null || !filter.match(permanent, getControllerId(), this, game)) {
            return false;
        }
        switch (setTargetPointer) {
            case TRIGGERED_CONTROLLER -> getEffects().setTargetPointer(new FixedTarget(permanent.getControllerId()));
            case TRIGGERED -> getEffects().setTargetPointer(new FixedTarget(permanent.getId(), game));
            case CONTROLLER -> getEffects().setTargetPointer(new FixedTarget(getControllerId()));
        }
        return true;
    }

    @Override
    public DelayedTriggeredAbility copy() {
        return new DelayedTriggeredManaAbility(this);
    }

    public DelayedTriggeredManaAbility withEventType(GameEvent.EventType eventType) {
        this.eventType = eventType;
        return this;
    }

    public DelayedTriggeredManaAbility withSetTargetPointer(SetTargetPointer setTargetPointer) {
        this.setTargetPointer = setTargetPointer;
        return this;
    }
}
