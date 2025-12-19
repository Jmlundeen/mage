
package mage.cards.s;

import mage.MageInt;
import mage.MageItem;
import mage.abilities.Ability;
import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.effects.common.DamageControllerEffect;
import mage.abilities.triggers.BeginningOfUpkeepTriggeredAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.target.common.TargetOpponent;

import java.util.List;
import java.util.UUID;

/**
 *
 * @author TGower
 */
public final class SleeperAgent extends CardImpl {

    public SleeperAgent(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId,setInfo,new CardType[]{CardType.CREATURE},"{B}");
        this.subtype.add(SubType.PHYREXIAN);
        this.subtype.add(SubType.MINION);
        this.power = new MageInt(3);
        this.toughness = new MageInt(3);

        // When Sleeper Agent enters the battlefield, target opponent gains control of it.
                Ability ability = new EntersBattlefieldTriggeredAbility(new SleeperAgentChangeControlEffect(), false);
                ability.addTarget(new TargetOpponent());
                this.addAbility(ability);
        // At the beginning of your upkeep, Sleeper Agent deals 2 damage to you.
                this.addAbility(new BeginningOfUpkeepTriggeredAbility(new DamageControllerEffect(2)));
    }

    private SleeperAgent(final SleeperAgent card) {
        super(card);
    }

    @Override
    public SleeperAgent copy() {
        return new SleeperAgent(this);
    }
}

class SleeperAgentChangeControlEffect extends ContinuousEffectImpl {

    SleeperAgentChangeControlEffect() {
        super(Duration.Custom, Layer.ControlChangingEffects_2, SubLayer.NA, Outcome.GainControl);
        staticText = "target opponent gains control of it";
    }

    private SleeperAgentChangeControlEffect(final SleeperAgentChangeControlEffect effect) {
        super(effect);
    }

    @Override
    public SleeperAgentChangeControlEffect copy() {
        return new SleeperAgentChangeControlEffect(this);
    }

    @Override
    public void applyToObjects(Layer layer, SubLayer sublayer, Ability source, Game game, List<MageItem> affectedObjects) {
        for (MageItem object : affectedObjects) {
            ((Permanent) object).changeControllerId(source.getFirstTarget(), game, source);
        }
    }

    @Override
    public boolean queryAffectedObjects(Layer layer, Ability source, Game game, List<MageItem> affectedObjects) {
        Permanent permanent = source.getSourcePermanentIfItStillExists(game);
        if (permanent != null) {
            affectedObjects.add(permanent);
            return true;
        } else {
            discard();
            return false;
        }
    }
}
