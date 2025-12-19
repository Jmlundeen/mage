
package mage.cards.g;

import mage.MageInt;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.condition.common.RevoltCondition;
import mage.abilities.decorator.ConditionalReplacementEffect;
import mage.abilities.effects.common.continuous.replacement.EntersWithCountersEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.AbilityWord;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.counters.CounterType;
import mage.watchers.common.RevoltWatcher;

import java.util.UUID;

/**
 * @author JRHerlehy
 */
public final class GreenwheelLiberator extends CardImpl {

    public GreenwheelLiberator(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{1}{G}");

        this.subtype.add(SubType.ELF);
        this.subtype.add(SubType.WARRIOR);
        this.power = new MageInt(2);
        this.toughness = new MageInt(1);

        // <i>Revolt</i> &mdash; Greenbelt Liberator enters the battlefield with two +1/+1 counters on it if a
        // permanent you controlled left the battlefield this turn.
        this.addAbility(new SimpleStaticAbility(new ConditionalReplacementEffect(
                new EntersWithCountersEffect(CounterType.P1P1.createInstance(2)),
                RevoltCondition.instance)
                .setText ("{this} enters with two +1/+1 counters on it " +
                "if a permanent you controlled left the battlefield this turn.")
        ).setAbilityWord(AbilityWord.REVOLT).addHint(RevoltCondition.getHint()), new RevoltWatcher());
    }

    private GreenwheelLiberator(final GreenwheelLiberator card) {
        super(card);
    }

    @Override
    public GreenwheelLiberator copy() {
        return new GreenwheelLiberator(this);
    }
}
