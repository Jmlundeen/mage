package mage.cards.s;

import mage.MageInt;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.dynamicvalue.common.CreaturesDiedThisTurnCount;
import mage.abilities.effects.common.continuous.replacement.EntersWithCountersEffect;
import mage.abilities.hint.common.CreaturesDiedThisTurnHint;
import mage.abilities.keyword.FlashAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.counters.CounterType;

import java.util.UUID;

/**
 * @author TheElk801
 */
public final class SautekhImmortal extends CardImpl {

    public SautekhImmortal(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT, CardType.CREATURE}, "{2}{B}");

        this.subtype.add(SubType.NECRON);
        this.power = new MageInt(2);
        this.toughness = new MageInt(2);

        // Flash
        this.addAbility(FlashAbility.getInstance());

        // Elite Troops -- Sautekh Immortal enters the battlefield with a +1/+1 counter on it for each creature that died this turn.
        this.addAbility(new SimpleStaticAbility(
                new EntersWithCountersEffect(CounterType.P1P1, CreaturesDiedThisTurnCount.instance)
        ).withFlavorWord("Elite Troops").addHint(CreaturesDiedThisTurnHint.instance));
    }

    private SautekhImmortal(final SautekhImmortal card) {
        super(card);
    }

    @Override
    public SautekhImmortal copy() {
        return new SautekhImmortal(this);
    }
}
