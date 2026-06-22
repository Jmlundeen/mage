
package mage.cards.s;

import mage.abilities.Ability;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.ContinuousEffect;
import mage.abilities.effects.common.AttachEffect;
import mage.abilities.effects.common.continuous.generic.GenericContinuousEffect;
import mage.abilities.keyword.EnchantAbility;
import mage.abilities.mana.AnyColorManaAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.target.TargetPermanent;
import mage.target.common.TargetLandPermanent;

import java.util.UUID;

/**
 *
 * @author LevelX2
 */
public final class ShelteredAerie extends CardImpl {

    public ShelteredAerie(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId,setInfo,new CardType[]{CardType.ENCHANTMENT},"{2}{G}");
        this.subtype.add(SubType.AURA);

        // Enchant land
        TargetPermanent auraTarget = new TargetLandPermanent();
        this.getSpellAbility().addTarget(auraTarget);
        this.getSpellAbility().addEffect(new AttachEffect(Outcome.Benefit));
        Ability ability = new EnchantAbility(auraTarget);
        this.addAbility(ability);

        // Enchanted land has "{T}: Add two mana of any one color."
        ContinuousEffect effect = new GenericContinuousEffect(Duration.WhileOnBattlefield, Outcome.AddAbility)
                .setAffected(ContinuousAffected.ATTACHED_TO)
                .withGainedAbilities(new AnyColorManaAbility(2))
                .setText("Enchanted land has \"{T}: Add two mana of any one color.\"");
        this.addAbility(new SimpleStaticAbility(effect));

    }

    private ShelteredAerie(final ShelteredAerie card) {
        super(card);
    }

    @Override
    public ShelteredAerie copy() {
        return new ShelteredAerie(this);
    }
}
