
package mage.cards.s;

import mage.MageInt;
import mage.MageItem;
import mage.abilities.Ability;
import mage.abilities.common.LandfallAbility;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.keyword.DefenderAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.game.Game;
import mage.game.permanent.Permanent;

import java.util.List;
import java.util.UUID;

/**
 *
 * @author jeffwadsworth
 */
public final class ShoalSerpent extends CardImpl {
    
    public ShoalSerpent(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId,setInfo,new CardType[]{CardType.CREATURE},"{5}{U}");
        this.subtype.add(SubType.SERPENT);

        this.power = new MageInt(5);
        this.toughness = new MageInt(5);

        // Defender
        this.addAbility(DefenderAbility.getInstance());
        
        // Landfall - Whenever a land you control enters, Shoal Serpent loses defender until end of turn.
        Ability ability = new LandfallAbility(Zone.BATTLEFIELD, new ShoalSerpentEffect(), false);
        this.addAbility(ability);
    }

    private ShoalSerpent(final ShoalSerpent card) {
        super(card);
    }

    @Override
    public ShoalSerpent copy() {
        return new ShoalSerpent(this);
    }
}

class ShoalSerpentEffect extends ContinuousEffectImpl {

    ShoalSerpentEffect() {
        super(Duration.EndOfTurn, Layer.AbilityAddingRemovingEffects_6, SubLayer.NA, Outcome.AddAbility);
        staticText = "{this} loses defender until end of turn";
    }

    private ShoalSerpentEffect(final ShoalSerpentEffect effect) {
        super(effect);
    }

    @Override
    public ShoalSerpentEffect copy() {
        return new ShoalSerpentEffect(this);
    }

    @Override
    public void applyToObjects(Layer layer, SubLayer sublayer, Ability source, Game game, List<MageItem> affectedObjects) {
        for (MageItem object : affectedObjects) {
            ((Permanent) object).removeAbility(DefenderAbility.getInstance(), source.getSourceId(), game);
        }
    }

    @Override
    public boolean queryAffectedObjects(Layer layer, Ability source, Game game, List<MageItem> affectedObjects) {
        Permanent permanent = game.getPermanent(source.getSourceId());
        if (permanent != null) {
            affectedObjects.add(permanent);
            return true;
        }
        return false;
    }
}
