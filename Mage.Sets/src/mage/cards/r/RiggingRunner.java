package mage.cards.r;

import mage.MageInt;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.condition.common.RaidCondition;
import mage.abilities.decorator.ConditionalReplacementEffect;
import mage.abilities.effects.common.continuous.replacement.EntersWithCountersEffect;
import mage.abilities.hint.common.RaidHint;
import mage.abilities.keyword.FirstStrikeAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.AbilityWord;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.counters.CounterType;
import mage.watchers.common.PlayerAttackedWatcher;

import java.util.UUID;

/**
 * @author LevelX2
 */
public final class RiggingRunner extends CardImpl {

    public RiggingRunner(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{R}");

        this.subtype.add(SubType.GOBLIN);
        this.subtype.add(SubType.PIRATE);
        this.power = new MageInt(1);
        this.toughness = new MageInt(1);

        // First strike
        this.addAbility(FirstStrikeAbility.getInstance());

        // Raid — Rigging Runner enters the battlefield with a +1/+1 counter on it if you attacked this turn.
        this.addAbility(new SimpleStaticAbility(new ConditionalReplacementEffect(
                new EntersWithCountersEffect(CounterType.P1P1.createInstance(1)),
                        RaidCondition.instance)
                        .setText("{this} enters with a +1/+1 counter on it if you attacked this turn"))
                        .setAbilityWord(AbilityWord.RAID)
                        .addHint(RaidHint.instance),
                new PlayerAttackedWatcher()
        );
    }

    private RiggingRunner(final RiggingRunner card) {
        super(card);
    }

    @Override
    public RiggingRunner copy() {
        return new RiggingRunner(this);
    }
}
