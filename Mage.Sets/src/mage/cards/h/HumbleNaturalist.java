package mage.cards.h;

import mage.MageInt;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.abilities.mana.conditional.FilteredSpellManaCondition;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.filter.StaticTypedFilters;

import java.util.UUID;

/**
 * @author TheElk801
 */
public final class HumbleNaturalist extends CardImpl {

    public HumbleNaturalist(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{1}{G}");

        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.DRUID);
        this.power = new MageInt(1);
        this.toughness = new MageInt(3);

        // {T}: Add one mana of any color. Spend this mana only to cast a creature spell.
        this.addAbility(ComposedManaAbilityBuilder.builder()
                .cost(new TapSourceCost())
                .addAnyColor(1)
                .condition(new FilteredSpellManaCondition(StaticTypedFilters.A_CREATURE_SPELL))
                .ruleText("Add one mana of any color. Spend this mana only to cast a creature spell")
                .build()
        );
    }

    private HumbleNaturalist(final HumbleNaturalist card) {
        super(card);
    }

    @Override
    public HumbleNaturalist copy() {
        return new HumbleNaturalist(this);
    }
}
