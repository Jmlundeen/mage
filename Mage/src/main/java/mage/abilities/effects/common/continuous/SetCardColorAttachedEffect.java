
package mage.abilities.effects.common.continuous;

import mage.MageItem;
import mage.ObjectColor;
import mage.abilities.Ability;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.constants.*;
import mage.game.Game;
import mage.game.permanent.Permanent;

import java.util.List;

/**
 * @author nantuko
 */
public class SetCardColorAttachedEffect extends ContinuousEffectImpl {

    private ObjectColor setColor;
    private AttachmentType attachmentType;

    public SetCardColorAttachedEffect(ObjectColor setColor, Duration duration, AttachmentType attachmentType) {
        super(duration, Layer.ColorChangingEffects_5, SubLayer.NA, Outcome.Benefit);
        this.setColor = setColor;
        this.attachmentType = attachmentType;
        setText();
    }

    protected SetCardColorAttachedEffect(final SetCardColorAttachedEffect effect) {
        super(effect);
        this.setColor = effect.setColor;
        this.attachmentType = effect.attachmentType;
    }

    @Override
    public void applyToObjects(Layer layer, SubLayer sublayer, Ability source, Game game, List<MageItem> affectedObjects) {
        for (MageItem object : affectedObjects) {
            ((Permanent) object).getColor(game).setColor(setColor);
        }
    }

    @Override
    public boolean queryAffectedObjects(Layer layer, Ability source, Game game, List<MageItem> affectedObjects) {
        Permanent equipment = game.getPermanent(source.getSourceId());
        if (equipment == null || equipment.getAttachedTo() == null) {
            return false;
        }
        Permanent target = game.getPermanent(equipment.getAttachedTo());
        if (target == null) {
            return false;
        }
        affectedObjects.add(target);
        return true;
    }

    @Override
    public SetCardColorAttachedEffect copy() {
        return new SetCardColorAttachedEffect(this);
    }

    private void setText() {
        StringBuilder sb = new StringBuilder();
        sb.append(attachmentType.verb());
        sb.append(" creature is ").append(setColor.getDescription());
        staticText = sb.toString();
    }
}
