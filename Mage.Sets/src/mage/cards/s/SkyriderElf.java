
package mage.cards.s;

import mage.MageInt;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.dynamicvalue.common.ColorsOfManaSpentToCastCount;
import mage.abilities.effects.common.continuous.replacement.EntersWithCountersEffect;
import mage.abilities.keyword.FlyingAbility;
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
public final class SkyriderElf extends CardImpl {

    public SkyriderElf(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId,setInfo,new CardType[]{CardType.CREATURE},"{X}{G}{U}");
        this.subtype.add(SubType.ELF);
        this.subtype.add(SubType.WARRIOR);
        this.subtype.add(SubType.ALLY);
        this.power = new MageInt(0);
        this.toughness = new MageInt(0);

        // Flying
        this.addAbility(FlyingAbility.getInstance());

        // <i>Converge</i>-Skyrider Elf enters the battlefield with a +1/+1 counter on it for each color of mana spent to cast it.
        this.addAbility(new SimpleStaticAbility(new EntersWithCountersEffect(CounterType.P1P1, ColorsOfManaSpentToCastCount.getInstance())
                .setText("{this} enters with a +1/+1 counter on it for each color of mana spent to cast it"))
                .setAbilityWord(AbilityWord.CONVERGE)
        );
    }

    private SkyriderElf(final SkyriderElf card) {
        super(card);
    }

    @Override
    public SkyriderElf copy() {
        return new SkyriderElf(this);
    }
}
