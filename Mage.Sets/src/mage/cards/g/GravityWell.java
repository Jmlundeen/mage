
package mage.cards.g;

import mage.MageItem;
import mage.abilities.Ability;
import mage.abilities.TriggeredAbilityImpl;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.effects.Effect;
import mage.abilities.keyword.FlyingAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.game.permanent.Permanent;
import mage.target.targetpointer.FixedTarget;

import java.util.List;
import java.util.UUID;

/**
 *
 * @author jeffwadsworth
 */
public final class GravityWell extends CardImpl {

    public GravityWell(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId,setInfo,new CardType[]{CardType.ENCHANTMENT},"{1}{G}{G}");

        // Whenever a creature with flying attacks, it loses flying until end of turn.
        this.addAbility(new GravityWellTriggeredAbility());
    }

    private GravityWell(final GravityWell card) {
        super(card);
    }

    @Override
    public GravityWell copy() {
        return new GravityWell(this);
    }
}

class GravityWellTriggeredAbility extends TriggeredAbilityImpl {

    public GravityWellTriggeredAbility() {
        super(Zone.BATTLEFIELD, new GravityWellEffect());
        setTriggerPhrase("Whenever a creature with flying attacks, ");
    }

    private GravityWellTriggeredAbility(final GravityWellTriggeredAbility ability) {
        super(ability);
    }

    @Override
    public boolean checkEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.ATTACKER_DECLARED;
    }

    @Override
    public boolean checkTrigger(GameEvent event, Game game) {
        Permanent attacker = game.getPermanent(event.getSourceId());
        if (attacker != null && attacker.getAbilities().contains(FlyingAbility.getInstance())) {
            for (Effect effect : getEffects()) {
                effect.setTargetPointer(new FixedTarget(event.getSourceId(), game));
            }
            return true;
        }
        return false;
    }

    @Override
    public GravityWellTriggeredAbility copy() {
        return new GravityWellTriggeredAbility(this);
    }
}

class GravityWellEffect extends ContinuousEffectImpl {

    GravityWellEffect() {
        super(Duration.EndOfTurn, Layer.AbilityAddingRemovingEffects_6, SubLayer.NA, Outcome.LoseAbility);
        staticText = "it loses flying until end of turn";
    }

    private GravityWellEffect(final GravityWellEffect effect) {
        super(effect);
    }

    @Override
    public GravityWellEffect copy() {
        return new GravityWellEffect(this);
    }

    @Override
    public void applyToObjects(Layer layer, SubLayer sublayer, Ability source, Game game, List<MageItem> affectedObjects) {
        for (MageItem object : affectedObjects) {
            ((Permanent) object).removeAbility(FlyingAbility.getInstance(), source.getSourceId(), game);
        }
    }

    @Override
    public boolean queryAffectedObjects(Layer layer, Ability source, Game game, List<MageItem> affectedObjects) {
        Permanent permanent = game.getPermanent(getTargetPointer().getFirst(game, source));
        if (permanent != null) {
            affectedObjects.add(permanent);
            return true;
        }
        return false;
    }
}
