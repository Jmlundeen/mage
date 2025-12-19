package mage.cards.g;

import mage.MageItem;
import mage.abilities.Ability;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.effects.common.AttachEffect;
import mage.abilities.effects.common.UntapSourceEffect;
import mage.abilities.effects.common.continuous.BoostEnchantedEffect;
import mage.abilities.effects.common.continuous.GainAbilityAttachedEffect;
import mage.abilities.keyword.EnchantAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.common.FilterCreaturePermanent;
import mage.filter.predicate.permanent.PermanentIdPredicate;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.target.TargetPermanent;
import mage.target.common.TargetCreaturePermanent;

import java.util.List;
import java.util.UUID;

/**
 * @author TheElk801
 */
public final class GauntletsOfLight extends CardImpl {

    public GauntletsOfLight(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ENCHANTMENT}, "{2}{W}");

        this.subtype.add(SubType.AURA);

        // Enchant creature
        TargetPermanent auraTarget = new TargetCreaturePermanent();
        this.getSpellAbility().addTarget(auraTarget);
        this.getSpellAbility().addEffect(new AttachEffect(Outcome.BoostCreature));
        Ability ability = new EnchantAbility(auraTarget);
        this.addAbility(ability);

        // Enchanted creature gets +0/+2 and assigns combat damage equal to its toughness rather than its power.
        ability = new SimpleStaticAbility(new BoostEnchantedEffect(0, 2));
        ability.addEffect(new GauntletsOfLightEffect());
        this.addAbility(ability);

        // Enchanted creature has "{2}{W}: Untap this creature."
        this.addAbility(new SimpleStaticAbility(
                new GainAbilityAttachedEffect(new SimpleActivatedAbility(
                        new UntapSourceEffect().setText("Untap this creature"), new ManaCostsImpl<>("{2}{W}")
                ), AttachmentType.AURA)
        ));
    }

    private GauntletsOfLight(final GauntletsOfLight card) {
        super(card);
    }

    @Override
    public GauntletsOfLight copy() {
        return new GauntletsOfLight(this);
    }
}

class GauntletsOfLightEffect extends ContinuousEffectImpl {

    GauntletsOfLightEffect() {
        super(Duration.WhileOnBattlefield, Layer.RulesEffects, SubLayer.NA, Outcome.Benefit);
        staticText = "and assigns combat damage equal to its toughness rather than its power";
    }

    private GauntletsOfLightEffect(final GauntletsOfLightEffect effect) {
        super(effect);
    }

    @Override
    public GauntletsOfLightEffect copy() {
        return new GauntletsOfLightEffect(this);
    }

    @Override
    public void applyToObjects(Layer layer, SubLayer sublayer, Ability source, Game game, List<MageItem> affectedObjects) {
        for (MageItem object : affectedObjects) {
            FilterCreaturePermanent filter = new FilterCreaturePermanent();
            filter.add(new PermanentIdPredicate(object.getId()));
            game.getCombat().setUseToughnessForDamage(true);
            game.getCombat().addUseToughnessForDamageFilter(filter);
        }
    }

    @Override
    public boolean queryAffectedObjects(Layer layer, Ability source, Game game, List<MageItem> affectedObjects) {
        Permanent permanent = game.getPermanent(source.getSourceId());
        if (permanent == null || permanent.getAttachedTo() == null) {
            return false;
        }
        Permanent attachedTo = game.getPermanent(permanent.getAttachedTo());
        if (attachedTo == null) {
            return false;
        }
        affectedObjects.add(attachedTo);
        return true;
    }
}
