package mage.cards.b;

import mage.abilities.Ability;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.condition.Condition;
import mage.abilities.condition.InvertCondition;
import mage.abilities.condition.common.AttachedToMatchesFilterCondition;
import mage.abilities.decorator.ConditionalContinuousEffect;
import mage.abilities.decorator.ConditionalRestrictionEffect;
import mage.abilities.effects.common.AttachEffect;
import mage.abilities.effects.common.combat.CantBlockAttachedEffect;
import mage.abilities.effects.common.continuous.BoostEnchantedEffect;
import mage.abilities.effects.common.continuous.SetBasePowerToughnessAttachedEffect;
import mage.abilities.keyword.EnchantAbility;
import mage.abilities.keyword.FlashAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.common.FilterControlledPermanent;
import mage.filter.common.FilterCreaturePermanent;
import mage.target.TargetPermanent;
import mage.target.common.TargetCreaturePermanent;

import java.util.UUID;

/**
 * @author TheElk801
 */
public final class BurdenOfProof extends CardImpl {

    private static final Condition condition = new InvertCondition(
            new AttachedToMatchesFilterCondition(new FilterControlledPermanent(SubType.DETECTIVE))
    );
    private static final FilterCreaturePermanent filter = new FilterCreaturePermanent(SubType.DETECTIVE, "");

    public BurdenOfProof(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ENCHANTMENT}, "{1}{U}");

        this.subtype.add(SubType.AURA);

        // Flash
        this.addAbility(FlashAbility.getInstance());

        // Enchant creature
        TargetPermanent auraTarget = new TargetCreaturePermanent();
        this.getSpellAbility().addTarget(auraTarget);
        this.getSpellAbility().addEffect(new AttachEffect(Outcome.BoostCreature));
        this.addAbility(new EnchantAbility(auraTarget));

        // Enchanted creature gets +2/+2 as long as it's a Detective you control. Otherwise, it has base power and toughness 1/1 and can't block Detectives.
        Ability ability = new SimpleStaticAbility(new ConditionalContinuousEffect(
                new BoostEnchantedEffect(2, 2),
                new SetBasePowerToughnessAttachedEffect(1, 1, AttachmentType.AURA),
                condition, "enchanted creature gets +2/+2 as long as it's a Detective you control. Otherwise, it has base power and toughness 1/1"
        ));
        ability.addEffect(new ConditionalRestrictionEffect(new CantBlockAttachedEffect(
                AttachmentType.AURA, Duration.WhileOnBattlefield, filter
        ), condition, "and can't block Detectives"));
        this.addAbility(ability);
    }

    private BurdenOfProof(final BurdenOfProof card) {
        super(card);
    }

    @Override
    public BurdenOfProof copy() {
        return new BurdenOfProof(this);
    }
}
