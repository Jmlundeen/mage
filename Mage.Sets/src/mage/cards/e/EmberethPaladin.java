package mage.cards.e;

import mage.MageInt;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.condition.common.AdamantCondition;
import mage.abilities.decorator.ConditionalReplacementEffect;
import mage.abilities.effects.common.continuous.replacement.EntersWithCountersEffect;
import mage.abilities.keyword.HasteAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.counters.CounterType;

import java.util.UUID;

/**
 * @author TheElk801
 */
public final class EmberethPaladin extends CardImpl {

    public EmberethPaladin(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{3}{R}");

        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.KNIGHT);
        this.power = new MageInt(4);
        this.toughness = new MageInt(1);

        // Haste
        this.addAbility(HasteAbility.getInstance());

        // Adamant — If at least three red mana was spent to cast this spell, Embereth Paladin enters the battlefield with a +1/+1 counter on it.
        this.addAbility(new SimpleStaticAbility(new ConditionalReplacementEffect(
                new EntersWithCountersEffect(CounterType.P1P1.createInstance()),
                AdamantCondition.RED)
                .setText("if at least three red mana was spent to cast this spell, " +
                        "{this} enters with a +1/+1 counter on it."))
                .withFlavorWord("Adamant")
        );
    }

    private EmberethPaladin(final EmberethPaladin card) {
        super(card);
    }

    @Override
    public EmberethPaladin copy() {
        return new EmberethPaladin(this);
    }
}
