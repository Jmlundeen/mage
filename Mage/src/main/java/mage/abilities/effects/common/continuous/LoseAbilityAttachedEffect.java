

package mage.abilities.effects.common.continuous;

import mage.MageItem;
import mage.abilities.Ability;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.constants.*;
import mage.game.Game;
import mage.game.permanent.Permanent;

import java.util.List;

/**
 * @author BetaSteward_at_googlemail.com
 */
public class LoseAbilityAttachedEffect extends ContinuousEffectImpl {

    protected Ability ability;
    protected AttachmentType attachmentType;

    public LoseAbilityAttachedEffect(Ability ability, AttachmentType attachmentType) {
        super(Duration.WhileOnBattlefield, Layer.AbilityAddingRemovingEffects_6, SubLayer.NA, Outcome.LoseAbility);
        this.ability = ability;
        this.attachmentType = attachmentType;
        setText();
    }

    protected LoseAbilityAttachedEffect(final LoseAbilityAttachedEffect effect) {
        super(effect);
        this.ability = effect.ability.copy();
        this.attachmentType = effect.attachmentType;
    }

    @Override
    public LoseAbilityAttachedEffect copy() {
        return new LoseAbilityAttachedEffect(this);
    }

    @Override
    public void applyToObjects(Layer layer, SubLayer sublayer, Ability source, Game game, List<MageItem> affectedObjects) {
        for (MageItem object : affectedObjects) {
            ((Permanent) object).removeAbility(ability, source.getSourceId(), game);
        }
    }

    @Override
    public boolean queryAffectedObjects(Layer layer, Ability source, Game game, List<MageItem> affectedObjects) {
        Permanent equipment = game.getPermanent(source.getSourceId());
        if (equipment != null && equipment.getAttachedTo() != null) {
            Permanent creature = game.getPermanent(equipment.getAttachedTo());
            if (creature != null) {
                affectedObjects.add(creature);
            }
        }
        return !affectedObjects.isEmpty();
    }

    private void setText() {
        StringBuilder sb = new StringBuilder();
        sb.append(attachmentType.verb());
        sb.append(" creature ");
        sb.append("loses ");
        sb.append(ability.getRule());
        staticText = sb.toString();
    }

}
