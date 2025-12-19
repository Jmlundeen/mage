package mage.cards.s;

import mage.MageItem;
import mage.abilities.Ability;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.effects.common.AttachEffect;
import mage.abilities.effects.common.continuous.SetBasePowerToughnessAttachedEffect;
import mage.abilities.keyword.DefenderAbility;
import mage.abilities.keyword.EnchantAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.target.TargetPermanent;
import mage.target.common.TargetCreaturePermanent;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * @author TheElk801
 */
public final class StasisField extends CardImpl {

    public StasisField(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ENCHANTMENT}, "{1}{U}");

        this.subtype.add(SubType.AURA);

        // Enchant creature
        TargetPermanent auraTarget = new TargetCreaturePermanent();
        this.getSpellAbility().addTarget(auraTarget);
        this.getSpellAbility().addEffect(new AttachEffect(Outcome.BoostCreature));
        this.addAbility(new EnchantAbility(auraTarget));

        // Enchanted creature has base power and toughness 0/2, has defender, and loses all other abilities.
        Ability ability = new SimpleStaticAbility(new SetBasePowerToughnessAttachedEffect(0, 2, AttachmentType.AURA));
        ability.addEffect(new StasisFieldEffect());
        this.addAbility(ability);
    }

    private StasisField(final StasisField card) {
        super(card);
    }

    @Override
    public StasisField copy() {
        return new StasisField(this);
    }
}

class StasisFieldEffect extends ContinuousEffectImpl {

    StasisFieldEffect() {
        super(Duration.WhileOnBattlefield, Layer.AbilityAddingRemovingEffects_6, SubLayer.NA, Outcome.Benefit);
        staticText = ", has defender, and loses all other abilities";
    }

    private StasisFieldEffect(final StasisFieldEffect effect) {
        super(effect);
    }

    @Override
    public StasisFieldEffect copy() {
        return new StasisFieldEffect(this);
    }

    @Override
    public void applyToObjects(Layer layer, SubLayer sublayer, Ability source, Game game, List<MageItem> affectedObjects) {
        for (MageItem object : affectedObjects) {
            Permanent permanent = (Permanent) object;
            permanent.removeAllAbilities(source.getSourceId(), game);
            permanent.addAbility(DefenderAbility.getInstance(), source.getSourceId(), game);
        }
    }

    @Override
    public boolean queryAffectedObjects(Layer layer, Ability source, Game game, List<MageItem> affectedObjects) {
        Permanent permanent = source.getSourcePermanentIfItStillExists(game);
        if (permanent == null || permanent.getAttachedTo() == null) {
            return false;
        }
        Permanent attachedTo = game.getPermanent(permanent.getAttachedTo());
        if (attachedTo != null) {
            affectedObjects.add(attachedTo);
            return true;
        }
        return false;
    }
}
