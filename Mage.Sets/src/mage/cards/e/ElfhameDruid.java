package mage.cards.e;

import mage.MageInt;
import mage.Mana;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.abilities.mana.GreenManaAbility;
import mage.abilities.mana.conditional.FilteredSpellManaCondition;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.filter.FilterTyped;
import mage.filter.predicate.typed.Spell.object.KickedSpellPredicate;

import java.util.UUID;

public final class ElfhameDruid extends CardImpl {

    private static final FilterTyped filter = new FilterTyped("kicked spell")
            .add(KickedSpellPredicate.instance);

    public ElfhameDruid(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{1}{G}");
        this.subtype.add(SubType.ELF);
        this.subtype.add(SubType.DRUID);

        this.power = new MageInt(0);
        this.toughness = new MageInt(2);

        // {T}: Add {G}.
        this.addAbility(new GreenManaAbility());

        // {T}: Add {G}{G}. Spend this mana only to cast kicked spells.
        this.addAbility(ComposedManaAbilityBuilder.builder()
                .cost(new TapSourceCost())
                .addStatic(Mana.GreenMana(2))
                .condition(new FilteredSpellManaCondition(filter))
                .ruleText("Add {G}{G}. Spend this mana only to cast kicked spells")
                .build()
        );
    }

    private ElfhameDruid(final ElfhameDruid card) {
        super(card);
    }

    @Override
    public ElfhameDruid copy() {
        return new ElfhameDruid(this);
    }


}
