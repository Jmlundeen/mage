package mage.cards.s;

import mage.MageInt;
import mage.abilities.costs.common.SacrificeTargetCost;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.dynamicvalue.common.SacrificeCostManaValue;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.abilities.mana.conditional.SpendOrActivateManaCondition;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.ManaType;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.filter.StaticFilters;
import mage.filter.StaticTypedFilters;

import java.util.UUID;

/**
 * @author TheElk801
 */
public final class SlobadIronGoblin extends CardImpl {

    public SlobadIronGoblin(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{2}{R}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.PHYREXIAN);
        this.subtype.add(SubType.GOBLIN);
        this.subtype.add(SubType.ARTIFICER);
        this.power = new MageInt(3);
        this.toughness = new MageInt(3);

        // {T}, Sacrifice an artifact: Add an amount of {R} equal to the sacrificed artifact's mana value. Spend this mana only to cast artifact spells or activate abilities of artifacts.
        this.addAbility(new ComposedManaAbilityBuilder()
                .cost(new TapSourceCost())
                .cost(new SacrificeTargetCost(StaticFilters.FILTER_CONTROLLED_PERMANENT_ARTIFACT_AN))
                .addDynamic(SacrificeCostManaValue.ARTIFACT, ManaType.RED)
                .condition(new SpendOrActivateManaCondition(StaticTypedFilters.AN_ARTIFACT))
                .ruleText("Add an amount of {R} equal to the sacrificed artifact's mana value. " +
                        "Spend this mana only to cast artifact spells or activate abilities of artifacts")
                .build()
        );
    }

    private SlobadIronGoblin(final SlobadIronGoblin card) {
        super(card);
    }

    @Override
    public SlobadIronGoblin copy() {
        return new SlobadIronGoblin(this);
    }
}
