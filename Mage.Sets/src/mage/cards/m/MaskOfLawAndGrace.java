
package mage.cards.m;

import mage.ObjectColor;
import mage.abilities.Ability;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.common.AttachEffect;
import mage.abilities.effects.common.continuous.generic.ContinuousEffectBuilder;
import mage.abilities.keyword.EnchantAbility;
import mage.abilities.keyword.ProtectionAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.ContinuousAffected;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.target.TargetPermanent;
import mage.target.common.TargetCreaturePermanent;

import java.util.UUID;

/**
 *
 * @author LoneFox
 */
public final class MaskOfLawAndGrace extends CardImpl {

    public MaskOfLawAndGrace(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId,setInfo,new CardType[]{CardType.ENCHANTMENT},"{W}");
        this.subtype.add(SubType.AURA);

        // Enchant creature
        TargetPermanent auraTarget = new TargetCreaturePermanent();
        this.getSpellAbility().addTarget(auraTarget);
        this.getSpellAbility().addEffect(new AttachEffect(Outcome.AddAbility));
        Ability ability = new EnchantAbility(auraTarget);
        this.addAbility(ability);
        // Enchanted creature has protection from black and from red.
        this.addAbility(new SimpleStaticAbility(new ContinuousEffectBuilder(Outcome.AddAbility, ContinuousAffected.ATTACHED_TO)
                .withGainedAbilities(ProtectionAbility.from(ObjectColor.BLACK, ObjectColor.RED))
                .setText("enchanted creature has protection from black and from red")
        ));
    }

    private MaskOfLawAndGrace(final MaskOfLawAndGrace card) {
        super(card);
    }

    @Override
    public MaskOfLawAndGrace copy() {
        return new MaskOfLawAndGrace(this);
    }
}
