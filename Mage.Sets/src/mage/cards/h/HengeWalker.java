package mage.cards.h;

import mage.MageInt;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.condition.common.AdamantCondition;
import mage.abilities.decorator.ConditionalReplacementEffect;
import mage.abilities.effects.common.continuous.replacement.EntersWithCountersEffect;
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
public final class HengeWalker extends CardImpl {

    public HengeWalker(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT, CardType.CREATURE}, "{3}");

        this.subtype.add(SubType.GOLEM);
        this.power = new MageInt(2);
        this.toughness = new MageInt(2);

        // Adamant — If at least three mana of the same color was spent to cast this spell, Henge Walker enters the battlefield with a +1/+1 counter on it.
        this.addAbility(new SimpleStaticAbility(new ConditionalReplacementEffect(
                new EntersWithCountersEffect(CounterType.P1P1.createInstance()),
                AdamantCondition.ANY)
                .setText("If at least three mana of the same color was spent to cast this spell, " +
                "{this} enters with a +1/+1 counter on it."))
                .setAbilityWord(AbilityWord.ADAMANT)
        );
    }

    private HengeWalker(final HengeWalker card) {
        super(card);
    }

    @Override
    public HengeWalker copy() {
        return new HengeWalker(this);
    }
}
