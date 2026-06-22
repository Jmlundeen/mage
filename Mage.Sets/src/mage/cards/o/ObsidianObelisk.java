package mage.cards.o;

import mage.abilities.common.EntersBattlefieldTappedAbility;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.mana.ColorlessManaAbility;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.abilities.mana.conditional.FilteredSpellManaCondition;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.filter.StaticTypedFilters;

import java.util.UUID;

/**
 * @author PurpleCrowbar
 */
public final class ObsidianObelisk extends CardImpl {

    public ObsidianObelisk(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT}, "{2}");

        // Obsidian Obelisk enters the battlefield tapped.
        this.addAbility(new EntersBattlefieldTappedAbility());

        // {T}: Add {C}.
        this.addAbility(new ColorlessManaAbility());

        // {T}: Add one mana of any color. Spend this mana only to cast a multicolored spell.
        this.addAbility(ComposedManaAbilityBuilder.builder()
                .cost(new TapSourceCost())
                .addAnyColor(1)
                .condition(new FilteredSpellManaCondition(StaticTypedFilters.A_MULTICOLORED_SPELL))
                .ruleText("Add one mana of any color. Spend this mana only to cast a multicolored spell.")
                .build()
        );
    }

    private ObsidianObelisk(final ObsidianObelisk card) {
        super(card);
    }

    @Override
    public ObsidianObelisk copy() {
        return new ObsidianObelisk(this);
    }
}
