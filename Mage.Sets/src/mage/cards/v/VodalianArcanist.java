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
import mage.filter.StaticTypedFilters;

import java.util.UUID;

/**
 * @author TheElk801
 */
public final class VodalianArcanist extends CardImpl {

    public VodalianArcanist(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{1}{U}");

        this.subtype.add(SubType.MERFOLK);
        this.subtype.add(SubType.WIZARD);
        this.power = new MageInt(1);
        this.toughness = new MageInt(3);

        // {T}: Add {C}. Spend this mana only to cast an instant or sorcery spell.
        this.addAbility(ComposedManaAbilityBuilder.builder()
                .cost(new TapSourceCost())
                .addStatic(Mana.ColorlessMana(1))
                .condition(new FilteredSpellManaCondition(StaticTypedFilters.AN_INSTANT_OR_SORCERY_SPELL))
                .ruleText("Add {C}. Spend this mana only to cast an instant or sorcery spell")
                .build()
        );
    }

    private VodalianArcanist(final VodalianArcanist card) {
        super(card);
    }

    @Override
    public VodalianArcanist copy() {
        return new VodalianArcanist(this);
    }
}
