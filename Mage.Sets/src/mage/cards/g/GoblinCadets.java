package mage.cards.g;

import mage.MageInt;
import mage.MageItem;
import mage.abilities.Ability;
import mage.abilities.common.BlocksOrBlockedSourceTriggeredAbility;
import mage.abilities.effects.ContinuousEffectImpl;
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
 * @author LevelX2
 */
public final class GoblinCadets extends CardImpl {

    public GoblinCadets(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId,setInfo,new CardType[]{CardType.CREATURE},"{R}");
        this.subtype.add(SubType.GOBLIN);

        this.power = new MageInt(2);
        this.toughness = new MageInt(1);

        // Whenever Goblin Cadets blocks or becomes blocked, target opponent gains control of it.
        Ability ability = new BlocksOrBlockedSourceTriggeredAbility(new GoblinCadetsChangeControlEffect());
        ability.addTarget(new TargetOpponent());
        this.addAbility(ability);

    }

    private GoblinCadets(final GoblinCadets card) {
        super(card);
    }

    @Override
    public GoblinCadets copy() {
        return new GoblinCadets(this);
    }
}

class GoblinCadetsChangeControlEffect extends ContinuousEffectImpl {

    GoblinCadetsChangeControlEffect() {
        super(Duration.Custom, Layer.ControlChangingEffects_2, SubLayer.NA, Outcome.GainControl);
        staticText = "target opponent gains control of it. <i>(This removes {this} from combat.)</i>";
    }

    private GoblinCadetsChangeControlEffect(final GoblinCadetsChangeControlEffect effect) {
        super(effect);
    }

    @Override
    public GoblinCadetsChangeControlEffect copy() {
        return new GoblinCadetsChangeControlEffect(this);
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
