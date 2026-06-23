package mage.cards.m;

import mage.abilities.Ability;
import mage.abilities.effects.common.AttachEffect;
import mage.abilities.keyword.EnchantAbility;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.abilities.mana.EnchantedTappedTriggeredManaAbility;
import mage.abilities.mana.providers.common.player.TargetPointerManaPlayerProvider;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.target.TargetPermanent;
import mage.target.common.TargetLandPermanent;

import java.util.UUID;

/**
 * @author LevelX2
 */
public final class MarketFestival extends CardImpl {

    public MarketFestival(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ENCHANTMENT}, "{3}{G}");
        this.subtype.add(SubType.AURA);

        // Enchant land
        TargetPermanent auraTarget = new TargetLandPermanent();
        this.getSpellAbility().addTarget(auraTarget);
        this.getSpellAbility().addEffect(new AttachEffect(Outcome.AddAbility));
        Ability ability = new EnchantAbility(auraTarget);
        this.addAbility(ability);

        // Whenever enchanted land is tapped for mana, its controller adds two mana in any combination of colors (in addition to the mana the land produces).
        this.addAbility(new EnchantedTappedTriggeredManaAbility(new ComposedManaAbilityBuilder()
                .addAnyCombination(2)
                .playerProvider(TargetPointerManaPlayerProvider.instance)
                .ruleText("its controller adds two mana in any combination of colors")
                .buildEffect())
        );
    }

    private MarketFestival(final MarketFestival card) {
        super(card);
    }

    @Override
    public MarketFestival copy() {
        return new MarketFestival(this);
    }
}
