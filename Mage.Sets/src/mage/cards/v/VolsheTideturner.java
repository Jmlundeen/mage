package mage.cards.v;

import mage.MageInt;
import mage.Mana;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.abilities.mana.conditional.FilteredSpellManaCondition;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.filter.FilterTyped;
import mage.filter.predicate.typed.LogicalPredicate;
import mage.filter.predicate.typed.Spell.SpellPredicate;
import mage.filter.predicate.typed.Spell.object.KickedSpellPredicate;

import java.util.UUID;

/**
 * @author TheElk801
 */
public final class VolsheTideturner extends CardImpl {

    private static final FilterTyped filter = new FilterTyped("an instant or sorcery spell or a kicked spell")
            .addAll(
                    SpellPredicate.instance,
                    LogicalPredicate.or(
                        KickedSpellPredicate.instance,
                        CardType.INSTANT.getPredicate(),
                        CardType.SORCERY.getPredicate()
                    )

            );

    public VolsheTideturner(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{1}{U}");

        this.subtype.add(SubType.MERFOLK);
        this.subtype.add(SubType.WIZARD);
        this.power = new MageInt(1);
        this.toughness = new MageInt(3);

        // {T}: Add {U}. Spend this mana only to cast an instant or sorcery spell or a kicked spell.
        this.addAbility(ComposedManaAbilityBuilder.builder()
                .cost(new TapSourceCost())
                .addStatic(Mana.BlueMana(1))
                .condition(new FilteredSpellManaCondition(filter))
                .ruleText("Add {U}. Spend this mana only to cast an instant or sorcery spell or a kicked spell")
                .build()
        );
    }

    private VolsheTideturner(final VolsheTideturner card) {
        super(card);
    }

    @Override
    public VolsheTideturner copy() {
        return new VolsheTideturner(this);
    }
}
