package mage.abilities.effects.common.continuous;

import mage.MageItem;
import mage.abilities.Ability;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.constants.*;
import mage.game.Game;
import mage.game.permanent.Permanent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author nantuko
 */
public class SetCardSubtypeAttachedEffect extends ContinuousEffectImpl {

    private List<SubType> setSubtypes = new ArrayList<>();
    private final AttachmentType attachmentType;

    public SetCardSubtypeAttachedEffect(Duration duration, AttachmentType attachmentType, SubType... setSubtype) {
        super(duration, Layer.TypeChangingEffects_4, SubLayer.NA, Outcome.Benefit);
        this.setSubtypes.addAll(Arrays.asList(setSubtype));
        this.attachmentType = attachmentType;
        this.setText();
    }

    protected SetCardSubtypeAttachedEffect(final SetCardSubtypeAttachedEffect effect) {
        super(effect);
        this.setSubtypes = effect.setSubtypes;
        this.attachmentType = effect.attachmentType;
    }

    @Override
    public void applyToObjects(Layer layer, SubLayer sublayer, Ability source, Game game, List<MageItem> affectedObjects) {
        for (MageItem object : affectedObjects) {
            Permanent permanent = (Permanent) object;
            permanent.removeAllCreatureTypes(game);
            permanent.addSubType(game, setSubtypes);
        }
    }

    @Override
    public boolean queryAffectedObjects(Layer layer, Ability source, Game game, List<MageItem> affectedObjects) {
        Permanent equipment = game.getPermanent(source.getSourceId());
        if (equipment == null || equipment.getAttachedTo() == null) {
            return true;
        }
        Permanent target = game.getPermanent(equipment.getAttachedTo());
        if (target == null) {
            return true;
        }
        affectedObjects.add(target);
        return true;
    }

    @Override
    public SetCardSubtypeAttachedEffect copy() {
        return new SetCardSubtypeAttachedEffect(this);
    }

    private void setText() {
        StringBuilder sb = new StringBuilder();
        sb.append(attachmentType.verb());
        sb.append(" creature is a");
        for (SubType subtype : this.setSubtypes) {
            sb.append(' ').append(subtype);
        }
        staticText = sb.toString();
    }
}
