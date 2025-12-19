package mage.abilities.effects.common.combat;

import mage.MageItem;
import mage.abilities.Ability;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.constants.*;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.util.CardUtil;

import java.util.List;

/**
 * @author mzulch
 */
public class CanBlockAdditionalCreatureAttachedEffect extends ContinuousEffectImpl {

    protected int amount;

    /**
     * Need to set text manually
     * Target can block an additional creature this turn
     */
    public CanBlockAdditionalCreatureAttachedEffect(AttachmentType attachmentType) {
        this(attachmentType, Duration.WhileOnBattlefield, 1);
    }

    /**
     * Need to set text manually
     * @param duration of effect
     * @param amount 0 = any number
     */
    public CanBlockAdditionalCreatureAttachedEffect(AttachmentType attachmentType, Duration duration, int amount) {
        super(duration, Layer.RulesEffects, SubLayer.NA, Outcome.Benefit);
        this.amount = amount;
        staticText = setText(attachmentType);
    }

    protected CanBlockAdditionalCreatureAttachedEffect(final CanBlockAdditionalCreatureAttachedEffect effect) {
        super(effect);
        this.amount = effect.amount;
    }

    @Override
    public CanBlockAdditionalCreatureAttachedEffect copy() {
        return new CanBlockAdditionalCreatureAttachedEffect(this);
    }

    @Override
    public void applyToObjects(Layer layer, SubLayer sublayer, Ability source, Game game, List<MageItem> affectedObjects) {
        for (MageItem object : affectedObjects) {
            Permanent permanent = (Permanent) object;
            // maxBlocks = 0 equals to "can block any number of creatures"
            if (amount > 0) {
                if (permanent.getMaxBlocks() > 0) {
                    permanent.setMaxBlocks(permanent.getMaxBlocks() + amount);
                }
            } else {
                permanent.setMaxBlocks(0);
            }
        }
    }

    @Override
    public boolean queryAffectedObjects(Layer layer, Ability source, Game game, List<MageItem> affectedObjects) {
        Permanent attachment = game.getPermanent(source.getSourceId());
        if (attachment == null) {
            return false;
        }
        Permanent permanent = game.getPermanent(attachment.getAttachedTo());
        if (permanent == null) {
            return false;
        }
        affectedObjects.add(permanent);
        return true;
    }

    private String setText(AttachmentType attachmentType) {
        StringBuilder sb = new StringBuilder(attachmentType.verb().toLowerCase());
        sb.append(" creature can block ");
        switch (amount) {
            case 0:
                sb.append("any number of creatures");
                break;
            case 1:
                sb.append("an additional creature each combat");
                break;
            default:
                sb.append(CardUtil.numberToText(amount));
                sb.append(" additional creatures");
        }
        return sb.toString();
    }
}
