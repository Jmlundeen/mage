package mage.cards.u;

import mage.abilities.Ability;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.common.AttachEffect;
import mage.abilities.effects.common.continuous.GainAbilityAttachedEffect;
import mage.abilities.effects.common.continuous.UntapSourceDuringEachOtherPlayersUntapStepEffect;
import mage.abilities.keyword.EnchantAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.target.TargetPermanent;
import mage.target.common.TargetLandPermanent;

import java.util.UUID;

/**
 * @author LevelX2
 */
public final class UrbanBurgeoning extends CardImpl {

    static final String rule = "Enchanted land has \"Untap this land during each other player's untap step.\"";

    public UrbanBurgeoning(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ENCHANTMENT}, "{G}");
        this.subtype.add(SubType.AURA);

        // Enchant land
        TargetPermanent auraTarget = new TargetLandPermanent();
        this.getSpellAbility().addTarget(auraTarget);
        this.getSpellAbility().addEffect(new AttachEffect(Outcome.BoostCreature));
        Ability ability = new EnchantAbility(auraTarget);
        this.addAbility(ability);

        // Enchanted land has "Untap this land during each other player's untap step."
        Ability gainedAbility = new SimpleStaticAbility(new UntapSourceDuringEachOtherPlayersUntapStepEffect());
        this.addAbility(new SimpleStaticAbility(new GainAbilityAttachedEffect(gainedAbility, AttachmentType.AURA, Duration.WhileOnBattlefield, rule)));
    }

    private UrbanBurgeoning(final UrbanBurgeoning card) {
        super(card);
    }

    @Override
    public UrbanBurgeoning copy() {
        return new UrbanBurgeoning(this);
    }
}
