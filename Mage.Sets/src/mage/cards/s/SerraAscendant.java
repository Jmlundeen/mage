
package mage.cards.s;

import mage.MageInt;
import mage.MageItem;
import mage.abilities.Ability;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.keyword.FlyingAbility;
import mage.abilities.keyword.LifelinkAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.players.Player;

import java.util.List;
import java.util.UUID;

/**
 *
 * @author BetaSteward_at_googlemail.com
 */
public final class SerraAscendant extends CardImpl {

    public SerraAscendant(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId,setInfo,new CardType[]{CardType.CREATURE},"{W}");
        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.MONK);

        this.power = new MageInt(1);
        this.toughness = new MageInt(1);

        // Lifelink (Damage dealt by this creature also causes you to gain that much life.)
        this.addAbility(LifelinkAbility.getInstance());

        // As long as you have 30 or more life, Serra Ascendant gets +5/+5 and has flying.
        this.addAbility(new SimpleStaticAbility(new SerraAscendantEffect()));
    }

    private SerraAscendant(final SerraAscendant card) {
        super(card);
    }

    @Override
    public SerraAscendant copy() {
        return new SerraAscendant(this);
    }

}

class SerraAscendantEffect extends ContinuousEffectImpl {

    SerraAscendantEffect() {
        super(Duration.WhileOnBattlefield, Outcome.BoostCreature);
        staticText = "As long as you have 30 or more life, {this} gets +5/+5 and has flying";
    }

    private SerraAscendantEffect(final SerraAscendantEffect effect) {
        super(effect);
    }

    @Override
    public SerraAscendantEffect copy() {
        return new SerraAscendantEffect(this);
    }

    @Override
    public void applyToObjects(Layer layer, SubLayer sublayer, Ability source, Game game, List<MageItem> affectedObjects) {
        for (MageItem object : affectedObjects) {
            Permanent creature = (Permanent) object;
            switch (layer) {
                case PTChangingEffects_7:
                    if (sublayer == SubLayer.ModifyPT_7c) {
                        creature.addPower(5);
                        creature.addToughness(5);
                    }
                    break;
                case AbilityAddingRemovingEffects_6:
                    if (sublayer == SubLayer.NA) {
                        creature.addAbility(FlyingAbility.getInstance(), source.getSourceId(), game);
                    }
                    break;
            }
        }
    }

    @Override
    public boolean queryAffectedObjects(Layer layer, Ability source, Game game, List<MageItem> affectedObjects) {
        Permanent creature = game.getPermanent(source.getSourceId());
        Player controller = game.getPlayer(source.getControllerId());
        if (creature != null && controller != null) {
            if (controller.getLife() >= 30) {
                affectedObjects.add(creature);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean hasLayer(Layer layer) {
        return Layer.AbilityAddingRemovingEffects_6 == layer || Layer.PTChangingEffects_7 == layer;
    }

    @Override
    public boolean hasSubLayer(SubLayer sublayer) {
        return sublayer == SubLayer.NA || sublayer == SubLayer.ModifyPT_7c;
    }
}
