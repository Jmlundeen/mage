
package mage.cards.w;

import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.Effect;
import mage.abilities.effects.common.AttachEffect;
import mage.abilities.effects.common.continuous.generic.GenericContinuousEffect;
import mage.abilities.effects.keyword.InvestigateEffect;
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
public final class WeirdingWood extends CardImpl {

    public WeirdingWood(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId,setInfo,new CardType[]{CardType.ENCHANTMENT},"{2}{G}");
        this.subtype.add(SubType.AURA);

        // Enchant land
        TargetPermanent auraTarget = new TargetLandPermanent();
        this.getSpellAbility().addTarget(auraTarget);
        this.getSpellAbility().addEffect(new AttachEffect(Outcome.Benefit));
        this.addAbility(new EnchantAbility(auraTarget));

        // When Weirding Wood enters the battlefield, investigate.
        this.addAbility(new EntersBattlefieldTriggeredAbility(new InvestigateEffect()));

        // Enchanted land has "{T}: Add two mana of any one color."
        Effect effect = new GenericContinuousEffect(Duration.WhileOnBattlefield, Outcome.AddAbility)
                .setAffected(ContinuousAffected.ATTACHED_TO)
                        .withGainedAbilities(new AnyColorManaAbility(2));
        effect.setText("Enchanted land has \"{T}: Add two mana of any one color.\"");
        this.addAbility(new SimpleStaticAbility(effect));
    }

    private WeirdingWood(final WeirdingWood card) {
        super(card);
    }

    @Override
    public WeirdingWood copy() {
        return new WeirdingWood(this);
    }
}
