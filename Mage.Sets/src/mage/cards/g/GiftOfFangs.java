package mage.cards.g;

import mage.abilities.Ability;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.condition.common.EnchantedCreatureSubtypeCondition;
import mage.abilities.decorator.ConditionalContinuousEffect;
import mage.abilities.effects.common.AttachEffect;
import mage.abilities.effects.common.continuous.BoostEnchantedEffect;
import mage.abilities.keyword.EnchantAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.target.TargetPermanent;
import mage.target.common.TargetCreaturePermanent;

import java.util.UUID;

/**
 * @author weirddan455
 */
public final class GiftOfFangs extends CardImpl {

    public GiftOfFangs(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ENCHANTMENT}, "{B}");

        this.subtype.add(SubType.AURA);

        // Enchant creature
        TargetPermanent auraTarget = new TargetCreaturePermanent();
        this.getSpellAbility().addTarget(auraTarget);
        this.getSpellAbility().addEffect(new AttachEffect(Outcome.Neutral));
        Ability ability = new EnchantAbility(auraTarget);
        this.addAbility(ability);

        // Enchanted creature gets +2/+2 as long as it's a Vampire. Otherwise, it gets -2/-2.
        this.addAbility(new SimpleStaticAbility(new ConditionalContinuousEffect(
                new BoostEnchantedEffect(2, 2),
                new BoostEnchantedEffect(-2, -2),
                new EnchantedCreatureSubtypeCondition(SubType.VAMPIRE),
                "Enchanted creature gets +2/+2 as long as it's a Vampire. Otherwise, it gets -2/-2"))
        );
    }

    private GiftOfFangs(final GiftOfFangs card) {
        super(card);
    }

    @Override
    public GiftOfFangs copy() {
        return new GiftOfFangs(this);
    }
}
