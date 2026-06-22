package mage.cards.l;

import mage.abilities.Ability;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.costs.mana.GenericManaCost;
import mage.abilities.effects.common.AttachEffect;
import mage.abilities.effects.common.continuous.generic.GenericContinuousEffect;
import mage.abilities.keyword.EnchantAbility;
import mage.abilities.keyword.WardAbility;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.abilities.mana.conditional.FilteredSpellManaCondition;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.FilterPermanent;
import mage.filter.common.FilterCreaturePermanent;
import mage.target.TargetPermanent;

import java.util.UUID;

/**
 * @author TheElk801
 */
public final class LeylineImmersion extends CardImpl {

    private static final FilterPermanent filter = new FilterCreaturePermanent("legendary creature");

    static {
        filter.add(SuperType.LEGENDARY.getPredicate());
    }

    public LeylineImmersion(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ENCHANTMENT}, "{3}{G}");

        this.subtype.add(SubType.AURA);

        // Enchant legendary creature
        TargetPermanent auraTarget = new TargetPermanent(filter);
        this.getSpellAbility().addTarget(auraTarget);
        this.getSpellAbility().addEffect(new AttachEffect(Outcome.BoostCreature));
        this.addAbility(new EnchantAbility(auraTarget));

        // Enchanted creature has ward {2} and "{T}: Add five mana in any combination of colors. Spend this mana only to cast spells."
        Ability manaAbility = ComposedManaAbilityBuilder.builder()
                .cost(new TapSourceCost())
                .addAnyCombination(5)
                .condition(new FilteredSpellManaCondition(null))
                .ruleText("Add five mana in any combination of colors. Spend this mana only to cast spells.")
                .build();
        this.addAbility(new SimpleStaticAbility(new GenericContinuousEffect(Duration.WhileOnBattlefield, Outcome.AddAbility)
                .setAffected(ContinuousAffected.ATTACHED_TO)
                .withGainedAbilities(new WardAbility(new GenericManaCost(2), false), manaAbility)
                .setText("enchanted creature has ward {2} and \"{T}: Add five mana in any combination of colors. Spend this mana only to cast spells.\"")
        ));
    }

    private LeylineImmersion(final LeylineImmersion card) {
        super(card);
    }

    @Override
    public LeylineImmersion copy() {
        return new LeylineImmersion(this);
    }
}
