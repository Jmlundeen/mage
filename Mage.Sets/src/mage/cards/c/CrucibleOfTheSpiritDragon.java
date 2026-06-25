package mage.cards.c;

import mage.abilities.Ability;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.costs.common.RemoveVariableCountersSourceCost;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.costs.mana.GenericManaCost;
import mage.abilities.dynamicvalue.common.CountersSourceCount;
import mage.abilities.dynamicvalue.common.GetXValue;
import mage.abilities.effects.common.counter.AddCountersSourceEffect;
import mage.abilities.mana.ColorlessManaAbility;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.abilities.mana.conditional.SpendOrActivateManaCondition;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.ManaType;
import mage.constants.SubType;
import mage.counters.CounterType;
import mage.filter.FilterTyped;
import mage.filter.predicate.typed.mageObject.IMageObjectPredicate;

import java.util.UUID;

/**
 *
 * @author LevelX2
 */
public final class CrucibleOfTheSpiritDragon extends CardImpl {

    static final FilterTyped filter = new FilterTyped("dragon")
            .add(IMageObjectPredicate.getOSPPredicate(SubType.DRAGON.getPredicate()));

    public CrucibleOfTheSpiritDragon(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.LAND}, "");

        // {T}: Add {C}.
        this.addAbility(new ColorlessManaAbility());

        // {1}, {T}: Put a storage counter on Crucible of the Spirit Dragon.
        Ability ability = new SimpleActivatedAbility(new AddCountersSourceEffect(CounterType.STORAGE.createInstance()), new GenericManaCost(1));
        ability.addCost(new TapSourceCost());
        this.addAbility(ability);

        // {T}, Remove X storage counters from Crucible of the Spirit Dragon: Add X mana in any combination of colors. Spend this mana only to cast Dragon spells or activate abilities of Dragons.
        this.addAbility(new ComposedManaAbilityBuilder()
                .cost(new TapSourceCost())
                .cost(new RemoveVariableCountersSourceCost(CounterType.STORAGE))
                .addDynamic(GetXValue.instance, ManaType.COLORLESS)
                .capacityOverride(new CountersSourceCount(CounterType.STORAGE))
                .condition(new SpendOrActivateManaCondition(filter))
                .ruleText("Add X mana in any combination of colors. Spend this mana only to cast Dragon spells or activate abilities of Dragons")
                .build()
        );
    }

    private CrucibleOfTheSpiritDragon(final CrucibleOfTheSpiritDragon card) {
        super(card);
    }

    @Override
    public CrucibleOfTheSpiritDragon copy() {
        return new CrucibleOfTheSpiritDragon(this);
    }
}
