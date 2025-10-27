package mage.cards.s;

import mage.MageInt;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.condition.common.RaidCondition;
import mage.abilities.decorator.ConditionalReplacementEffect;
import mage.abilities.effects.common.continuous.replacement.EntersWithCountersEffect;
import mage.abilities.hint.common.RaidHint;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.AbilityWord;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.counters.CounterType;
import mage.watchers.common.PlayerAttackedWatcher;

import java.util.UUID;

/**
 * @author JayDi85
 */
public final class SwaggeringCorsair extends CardImpl {

    public SwaggeringCorsair(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{2}{R}");

        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.PIRATE);
        this.power = new MageInt(2);
        this.toughness = new MageInt(2);

        // <i>Raid</i> &mdash; Swaggering Corsair enters the battlefield with a +1/+1 counter on it if you attacked this turn.
        this.addAbility(new SimpleStaticAbility(new ConditionalReplacementEffect(
                new EntersWithCountersEffect(CounterType.P1P1.createInstance()),
                RaidCondition.instance)
                .setText("{this} enters with a +1/+1 counter on it if you attacked this turn"))
                        .setAbilityWord(AbilityWord.RAID)
                        .addHint(RaidHint.instance),
                new PlayerAttackedWatcher()
        );
    }

    private SwaggeringCorsair(final SwaggeringCorsair card) {
        super(card);
    }

    @Override
    public SwaggeringCorsair copy() {
        return new SwaggeringCorsair(this);
    }
}
