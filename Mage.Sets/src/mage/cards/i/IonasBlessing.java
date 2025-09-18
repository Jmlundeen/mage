
package mage.cards.i;

import mage.MageItem;
import mage.abilities.Ability;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.effects.Effect;
import mage.abilities.effects.common.AttachEffect;
import mage.abilities.effects.common.continuous.BoostEnchantedEffect;
import mage.abilities.effects.common.continuous.GainAbilityAttachedEffect;
import mage.abilities.keyword.EnchantAbility;
import mage.abilities.keyword.VigilanceAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.target.TargetPermanent;
import mage.target.common.TargetCreaturePermanent;

import java.util.List;
import java.util.UUID;

/**
 *
 * @author fireshoes
 */
public final class IonasBlessing extends CardImpl {

    public IonasBlessing(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId,setInfo,new CardType[]{CardType.ENCHANTMENT},"{3}{W}");
        this.subtype.add(SubType.AURA);

        // Enchant creature
        TargetPermanent auraTarget = new TargetCreaturePermanent();
        this.getSpellAbility().addTarget(auraTarget);
        this.getSpellAbility().addEffect(new AttachEffect(Outcome.AddAbility));
        Ability ability = new EnchantAbility(auraTarget);
        this.addAbility(ability);

        // Enchanted creature gets +2/+2, has vigilance, and can block an additional creature.
        ability = new SimpleStaticAbility(new BoostEnchantedEffect(2, 2, Duration.WhileOnBattlefield));
        Effect effect = new GainAbilityAttachedEffect(VigilanceAbility.getInstance(), AttachmentType.AURA);
        effect.setText(", has vigilance");
        ability.addEffect(effect);
        ability.addEffect(new IonasBlessingEffect());
        this.addAbility(ability);
    }

    private IonasBlessing(final IonasBlessing card) {
        super(card);
    }

    @Override
    public IonasBlessing copy() {
        return new IonasBlessing(this);
    }
}

class IonasBlessingEffect extends ContinuousEffectImpl {

    IonasBlessingEffect() {
        super(Duration.WhileOnBattlefield, Layer.RulesEffects, SubLayer.NA, Outcome.Benefit);
        staticText = ", and can block an additional creature each combat";
    }

    private IonasBlessingEffect(final IonasBlessingEffect effect) {
        super(effect);
    }

    @Override
    public IonasBlessingEffect copy() {
        return new IonasBlessingEffect(this);
    }

    @Override
    public void applyToObjects(Layer layer, SubLayer sublayer, Ability source, Game game, List<MageItem> affectedObjects) {
        for (MageItem object : affectedObjects) {
            Permanent permanent = (Permanent) object;
            permanent.setMaxBlocks(permanent.getMaxBlocks() + 1);
        }
    }

    @Override
    public boolean queryAffectedObjects(Layer layer, Ability source, Game game, List<MageItem> affectedObjects) {
        Permanent perm = game.getPermanent(source.getSourceId());
        if (perm == null || perm.getAttachedTo() == null) {
            return false;
        }
        Permanent enchanted = game.getPermanent(perm.getAttachedTo());
        if (enchanted != null && enchanted.getMaxBlocks() > 0) {
            affectedObjects.add(enchanted);
            return true;
        }
        return false;
    }
}
