package mage.cards.l;

import mage.MageInt;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.condition.common.AdamantCondition;
import mage.abilities.decorator.ConditionalReplacementEffect;
import mage.abilities.effects.common.continuous.replacement.EntersWithCountersEffect;
import mage.abilities.keyword.MenaceAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.AbilityWord;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.counters.CounterType;

import java.util.UUID;

/**
 * @author TheElk801
 */
public final class LocthwainPaladin extends CardImpl {

    public LocthwainPaladin(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{3}{B}");

        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.KNIGHT);
        this.power = new MageInt(3);
        this.toughness = new MageInt(2);

        // Menace
        this.addAbility(new MenaceAbility());

        // Adamant — If at least three black mana was spent to cast this spell, Locthwain Paladin enters the battlefield with a +1/+1 counter on it.
        this.addAbility(new SimpleStaticAbility(new ConditionalReplacementEffect(
                new EntersWithCountersEffect(CounterType.P1P1.createInstance()),
                AdamantCondition.BLACK)
                .setText("if at least three black mana was spent to cast this spell, {this} enters with a +1/+1 counter on it")
        ).setAbilityWord(AbilityWord.ADAMANT));
    }

    private LocthwainPaladin(final LocthwainPaladin card) {
        super(card);
    }

    @Override
    public LocthwainPaladin copy() {
        return new LocthwainPaladin(this);
    }
}
