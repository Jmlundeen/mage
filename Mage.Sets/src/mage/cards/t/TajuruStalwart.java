
package mage.cards.t;

import mage.MageInt;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.dynamicvalue.common.ColorsOfManaSpentToCastCount;
import mage.abilities.effects.common.continuous.replacement.EntersWithCountersEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.AbilityWord;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.counters.CounterType;

import java.util.UUID;

/**
 *
 * @author LevelX2
 */
public final class TajuruStalwart extends CardImpl {

    public TajuruStalwart(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId,setInfo,new CardType[]{CardType.CREATURE},"{2}{G}");
        this.subtype.add(SubType.ELF);
        this.subtype.add(SubType.SCOUT);
        this.subtype.add(SubType.ALLY);
        this.power = new MageInt(0);
        this.toughness = new MageInt(1);

        // <i>Converge</i> &mdash; Tajuru Stalwart enters the battlefield with a +1/+1 counter on it for each color of mana spent to cast it.
        this.addAbility(new SimpleStaticAbility(new EntersWithCountersEffect(CounterType.P1P1, ColorsOfManaSpentToCastCount.getInstance())
                .setText("{this} enters with a +1/+1 counter on it for each color of mana spent to cast it")
        ).setAbilityWord(AbilityWord.CONVERGE));

    }

    private TajuruStalwart(final TajuruStalwart card) {
        super(card);
    }

    @Override
    public TajuruStalwart copy() {
        return new TajuruStalwart(this);
    }
}
