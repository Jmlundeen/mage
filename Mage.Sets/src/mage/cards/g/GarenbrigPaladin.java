package mage.cards.g;

import mage.MageInt;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.condition.common.AdamantCondition;
import mage.abilities.decorator.ConditionalReplacementEffect;
import mage.abilities.effects.common.continuous.replacement.EntersWithCountersEffect;
import mage.abilities.keyword.DauntAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.counters.CounterType;

import java.util.UUID;

/**
 * @author TheElk801
 */
public final class GarenbrigPaladin extends CardImpl {

    public GarenbrigPaladin(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{4}{G}");

        this.subtype.add(SubType.GIANT);
        this.subtype.add(SubType.KNIGHT);
        this.power = new MageInt(4);
        this.toughness = new MageInt(4);

        // Adamant — If at least three green mana was spent to cast this spell, Garenbrig Paladin enters the battlefield with a +1/+1 counter on it.
        this.addAbility(new SimpleStaticAbility(new ConditionalReplacementEffect(
                new EntersWithCountersEffect(
                CounterType.P1P1.createInstance()), AdamantCondition.GREEN)
                .setText("if at least three green mana was spent to cast this spell, " +
                        "{this} enters with a +1/+1 counter on it."))
                .withFlavorWord("Adamant")
        );

        // Garenbrig Paladin can't be blocked by creatures with power 2 or less.
        this.addAbility(new DauntAbility());
    }

    private GarenbrigPaladin(final GarenbrigPaladin card) {
        super(card);
    }

    @Override
    public GarenbrigPaladin copy() {
        return new GarenbrigPaladin(this);
    }
}
