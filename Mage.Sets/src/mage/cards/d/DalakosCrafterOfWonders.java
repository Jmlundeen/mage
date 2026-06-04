package mage.cards.d;

import mage.MageInt;
import mage.Mana;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.effects.common.continuous.generic.GenericContinuousEffect;
import mage.abilities.keyword.FlyingAbility;
import mage.abilities.keyword.HasteAbility;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.abilities.mana.conditional.SpendOrActivateManaCondition;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.FilterTyped;
import mage.filter.StaticTypedFilters;
import mage.filter.predicate.typed.permanent.status.EquippedPredicate;

import java.util.UUID;

/**
 * @author TheElk801
 */
public final class DalakosCrafterOfWonders extends CardImpl {

    private static final FilterTyped filter = new FilterTyped("Equipped creature you control")
            .addAll(
                    EquippedPredicate.instance,
                    TargetController.YOU.getControllerPredicate()
            );

    public DalakosCrafterOfWonders(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{1}{U}{R}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.MERFOLK);
        this.subtype.add(SubType.ARTIFICER);
        this.power = new MageInt(2);
        this.toughness = new MageInt(4);

        // {T}: Add {C}{C}. Spend this mana only to cast artifact spells or activate abilities of artifacts.
        this.addAbility(new ComposedManaAbilityBuilder()
                .cost(new TapSourceCost())
                .addStatic(Mana.ColorlessMana(2))
                .condition(new SpendOrActivateManaCondition(StaticTypedFilters.AN_ARTIFACT))
                .ruleText("Add {C}{C}. Spend this mana only to cast artifact spells or activate abilities of artifacts")
                .build()
        );

        // Equipped creatures you control have flying and haste.
        this.addAbility(new SimpleStaticAbility(new GenericContinuousEffect(Duration.WhileOnBattlefield, Outcome.AddAbility, filter, Zone.BATTLEFIELD)
                .withGainedAbilities(FlyingAbility.getInstance(), HasteAbility.getInstance())
                .setText("Equipped creatures you control have flying and haste")
        ));
    }

    private DalakosCrafterOfWonders(final DalakosCrafterOfWonders card) {
        super(card);
    }

    @Override
    public DalakosCrafterOfWonders copy() {
        return new DalakosCrafterOfWonders(this);
    }
}
