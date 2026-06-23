package mage.cards.e;

import mage.abilities.common.EntersBattlefieldTappedAbility;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.dynamicvalue.IntPlusDynamicValue;
import mage.abilities.dynamicvalue.common.CountersSourceCount;
import mage.abilities.effects.common.counter.AddCountersSourceEffect;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.abilities.mana.SimpleManaAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Zone;
import mage.counters.CounterType;

import java.util.UUID;

/**
 * @author TheElk801
 */
public final class EmpoweredAutogenerator extends CardImpl {

    public EmpoweredAutogenerator(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT}, "{4}");

        // Empowered Autogenerator enters the battlefield tapped.
        this.addAbility(new EntersBattlefieldTappedAbility());

        // {T}: Put a charge counter on Empowered Autogenerator. Add X mana of any one color, where X is the number of charge counters on Empowered Autogenerator.
        // Empowered Autogenerator's activated ability is a mana ability. It doesn’t use the stack and can’t be responded to. (2019-08-23)
        SimpleManaAbility ability = new SimpleManaAbility(Zone.BATTLEFIELD, new AddCountersSourceEffect(CounterType.CHARGE.createInstance()), new TapSourceCost());
        ability.addEffect(new ComposedManaAbilityBuilder()
                .addChoiceAnyOneColor(new CountersSourceCount(CounterType.CHARGE))
                .capacityOverride(new IntPlusDynamicValue(1, new CountersSourceCount(CounterType.CHARGE)))
                .ruleText("Add X mana of any one color, where X is the number of charge counters on {this}")
                .buildEffect()
        );
        this.addAbility(ability);
    }

    private EmpoweredAutogenerator(final EmpoweredAutogenerator card) {
        super(card);
    }

    @Override
    public EmpoweredAutogenerator copy() {
        return new EmpoweredAutogenerator(this);
    }
}
