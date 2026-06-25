package mage.cards.i;

import mage.MageInt;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.keyword.ReachAbility;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.abilities.mana.GreenManaAbility;
import mage.abilities.mana.conditional.FilteredSpellManaCondition;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.filter.FilterTyped;
import mage.filter.predicate.typed.LogicalPredicate;
import mage.filter.predicate.typed.Spell.SpellPredicate;
import mage.filter.predicate.typed.mageObject.IMageObjectPredicate;

import java.util.UUID;

/**
 * @author Susucr
 */
public final class IntrepidStablemaster extends CardImpl {

    private static final FilterTyped filter = new FilterTyped("Mount or Vehicle spells")
            .addAll(SpellPredicate.instance,
                    LogicalPredicate.or(IMageObjectPredicate.getOSPPredicate(SubType.MOUNT.getPredicate()), IMageObjectPredicate.getOSPPredicate(SubType.VEHICLE.getPredicate()))
            );

    public IntrepidStablemaster(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{1}{G}");

        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.SCOUT);
        this.power = new MageInt(2);
        this.toughness = new MageInt(2);

        // Reach
        this.addAbility(ReachAbility.getInstance());

        // {T}: Add {G}.
        this.addAbility(new GreenManaAbility());

        // {T}: Add two mana of any one color. Spend this mana only to cast Mount or Vehicle spells.
        this.addAbility(new ComposedManaAbilityBuilder()
                .cost(new TapSourceCost())
                .addAnyColor(2)
                .condition(new FilteredSpellManaCondition(filter))
                .ruleText("Add two mana of any one color. Spend this mana only to cast Mount or Vehicle spells.")
                .build()
        );
    }

    private IntrepidStablemaster(final IntrepidStablemaster card) {
        super(card);
    }

    @Override
    public IntrepidStablemaster copy() {
        return new IntrepidStablemaster(this);
    }
}
