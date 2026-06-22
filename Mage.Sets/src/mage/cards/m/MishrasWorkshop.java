package mage.cards.m;

import mage.Mana;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.abilities.mana.conditional.FilteredSpellManaCondition;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.filter.FilterTyped;
import mage.filter.predicate.typed.Spell.SpellPredicate;

import java.util.UUID;

/**
 *
 * @author LevelX2
 */
public final class MishrasWorkshop extends CardImpl {

    private static final FilterTyped filter = new FilterTyped("artifact spells")
            .addAll(
                    SpellPredicate.instance,
                    CardType.ARTIFACT.getPredicate()
            );

    public MishrasWorkshop(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId,setInfo,new CardType[]{CardType.LAND},"");

        // {tap}: Add {C}{C}{C}. Spend this mana only to cast artifact spells.
        this.addAbility(ComposedManaAbilityBuilder.builder()
                .cost(new TapSourceCost())
                .addStatic(Mana.ColorlessMana(3))
                .condition(new FilteredSpellManaCondition(filter))
                .ruleText("Add {C}{C}{C}. Spend this mana only to cast artifact spells.")
                .build()
        );
    }

    private MishrasWorkshop(final MishrasWorkshop card) {
        super(card);
    }

    @Override
    public MishrasWorkshop copy() {
        return new MishrasWorkshop(this);
    }
}
