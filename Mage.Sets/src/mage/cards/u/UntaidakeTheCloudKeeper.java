
package mage.cards.u;

import mage.Mana;
import mage.abilities.common.EntersBattlefieldTappedAbility;
import mage.abilities.costs.common.PayLifeCost;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.abilities.mana.conditional.FilteredSpellManaCondition;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SuperType;
import mage.filter.FilterTyped;
import mage.filter.predicate.typed.Spell.SpellPredicate;
import mage.filter.predicate.typed.mageObject.IMageObjectPredicate;

import java.util.UUID;

/**
 *
 * @author anonymous
 */
public final class UntaidakeTheCloudKeeper extends CardImpl {

    private static final FilterTyped filter = new FilterTyped("legendary spell")
            .addAll(SpellPredicate.instance,
                    IMageObjectPredicate.getOSPPredicate(SuperType.LEGENDARY.getPredicate())
            );

    public UntaidakeTheCloudKeeper(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId,setInfo,new CardType[]{CardType.LAND},"");
        this.supertype.add(SuperType.LEGENDARY);

        // Untaidake, the Cloud Keeper enters the battlefield tapped.
        this.addAbility(new EntersBattlefieldTappedAbility());

        // {T}, Pay 2 life: Add {C}{C}. Spend this mana only to cast legendary spells.
        this.addAbility(ComposedManaAbilityBuilder.builder()
                .cost(new TapSourceCost())
                .cost(new PayLifeCost(2))
                .addStatic(Mana.ColorlessMana(2))
                .condition(new FilteredSpellManaCondition(filter))
                .ruleText("Add {C}{C}. Spend this mana only to cast legendary spells")
                .build()
        );

    }

    private UntaidakeTheCloudKeeper(final UntaidakeTheCloudKeeper card) {
        super(card);
    }

    @Override
    public UntaidakeTheCloudKeeper copy() {
        return new UntaidakeTheCloudKeeper(this);
    }
}
