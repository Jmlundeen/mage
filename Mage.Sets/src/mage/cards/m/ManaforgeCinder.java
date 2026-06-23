package mage.cards.m;

import mage.MageInt;
import mage.abilities.costs.mana.GenericManaCost;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.ManaType;
import mage.constants.SubType;

import java.util.Set;
import java.util.UUID;

/**
 * @author Susucr
 */
public final class ManaforgeCinder extends CardImpl {

    public ManaforgeCinder(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{B/R}");
        this.subtype.add(SubType.ELEMENTAL);
        this.subtype.add(SubType.SHAMAN);
        this.power = new MageInt(1);
        this.toughness = new MageInt(1);

        // {1}: Add {B} or {R}. Activate this ability no more than three times each turn.
        this.addAbility(ComposedManaAbilityBuilder.builder()
                .cost(new GenericManaCost(1))
                .addChoice(Set.of(ManaType.BLACK, ManaType.RED), 1)
                .maxActivations(3)
                .ruleText("add {B} or {R}. Activate this ability no more than three times each turn.")
                .build());
    }

    private ManaforgeCinder(final ManaforgeCinder card) {
        super(card);
    }

    @Override
    public ManaforgeCinder copy() {
        return new ManaforgeCinder(this);
    }
}
