package mage.cards.l;

import mage.MageInt;
import mage.abilities.common.LandfallAbility;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;

import java.util.UUID;

/**
 * @author BetaSteward_at_googlemail.com
 */
public final class LotusCobra extends CardImpl {

    public LotusCobra(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{1}{G}");
        this.subtype.add(SubType.SNAKE);

        this.power = new MageInt(2);
        this.toughness = new MageInt(1);

        // Landfall — Whenever a land you control enters, you may add one mana of any color.
        this.addAbility(new LandfallAbility(ComposedManaAbilityBuilder.builder()
                .addAnyColor(1)
                .ruleText("add one mana of any color")
                .buildEffect()
                ,false)
        );
    }

    private LotusCobra(final LotusCobra card) {
        super(card);
    }

    @Override
    public LotusCobra copy() {
        return new LotusCobra(this);
    }
}
