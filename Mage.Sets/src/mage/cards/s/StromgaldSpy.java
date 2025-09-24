
package mage.cards.s;

import mage.MageInt;
import mage.MageItem;
import mage.abilities.Ability;
import mage.abilities.common.AttacksAndIsNotBlockedTriggeredAbility;
import mage.abilities.condition.common.SourceRemainsInZoneCondition;
import mage.abilities.decorator.ConditionalContinuousEffect;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.effects.common.continuous.AssignNoCombatDamageSourceEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.players.Player;

import java.util.List;
import java.util.UUID;

/**
 * @author L_J
 */
public final class StromgaldSpy extends CardImpl {

    public StromgaldSpy(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{3}{B}");
        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.ROGUE);
        this.power = new MageInt(2);
        this.toughness = new MageInt(4);

        // Whenever Stromgald Spy attacks and isn't blocked, you may have defending player play with their hand revealed
        // for as long as Stromgald Spy remains on the battlefield. If you do, Stromgald Spy assigns no combat damage this turn.
        Ability ability = new AttacksAndIsNotBlockedTriggeredAbility(
                new ConditionalContinuousEffect(
                        new StromgaldSpyEffect(),
                        new SourceRemainsInZoneCondition(Zone.BATTLEFIELD),
                        "you may have defending player play with their hand revealed for as long as {this} remains on the battlefield"),
                true, SetTargetPointer.PLAYER);
        ability.addEffect(new AssignNoCombatDamageSourceEffect(Duration.EndOfTurn, true));
        this.addAbility(ability);
    }

    private StromgaldSpy(final StromgaldSpy card) {
        super(card);
    }

    @Override
    public StromgaldSpy copy() {
        return new StromgaldSpy(this);
    }
}

class StromgaldSpyEffect extends ContinuousEffectImpl {

    StromgaldSpyEffect() {
        super(Duration.Custom, Layer.PlayerEffects, SubLayer.NA, Outcome.Detriment);
    }

    private StromgaldSpyEffect(final StromgaldSpyEffect effect) {
        super(effect);
    }

    @Override
    public void applyToObjects(Layer layer, SubLayer sublayer, Ability source, Game game, List<MageItem> affectedObjects) {
        for (MageItem object : affectedObjects) {
            Player defender = (Player) object;
            defender.revealCards(defender.getName() + "'s hand cards", defender.getHand(), game, false);
        }
    }

    @Override
    public boolean queryAffectedObjects(Layer layer, Ability source, Game game, List<MageItem> affectedObjects) {
        Permanent sourcePermanent = game.getPermanent(source.getSourceId());
        Player controller = game.getPlayer(source.getControllerId());
        if (controller != null && sourcePermanent != null) {
            Player defender = game.getPlayer(this.getTargetPointer().getFirst(game, source));
            if (defender != null) {
                affectedObjects.add(defender);
                return true;
            }
        }
        return false;
    }

    @Override
    public StromgaldSpyEffect copy() {
        return new StromgaldSpyEffect(this);
    }
}
