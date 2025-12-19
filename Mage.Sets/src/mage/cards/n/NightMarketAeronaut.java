package mage.cards.n;

import mage.MageInt;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.condition.common.RevoltCondition;
import mage.abilities.decorator.ConditionalReplacementEffect;
import mage.abilities.effects.common.continuous.replacement.EntersWithCountersEffect;
import mage.abilities.keyword.FlyingAbility;
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
public final class NightMarketAeronaut extends CardImpl {

    public NightMarketAeronaut(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{3}{B}");

        this.subtype.add(SubType.AETHERBORN);
        this.subtype.add(SubType.WARRIOR);
        this.power = new MageInt(2);
        this.toughness = new MageInt(2);

        // Flying
        this.addAbility(FlyingAbility.getInstance());

        // <i>Revolt</i> &mdash; Night Market Aeronaut enters the battlefield with a +1/+1 counter on it if
        // a permanent you controlled left the battlefield this turn.
        this.addAbility(new SimpleStaticAbility(new ConditionalReplacementEffect(
                new EntersWithCountersEffect(CounterType.P1P1.createInstance()),
                RevoltCondition.instance)
                .setText("{this} enters with a +1/+1 counter on it if a permanent you controlled left the battlefield this turn.")
        ).setAbilityWord(AbilityWord.REVOLT).addHint(RevoltCondition.getHint()), new RevoltWatcher());
    }

    private NightMarketAeronaut(final NightMarketAeronaut card) {
        super(card);
    }

    @Override
    public NightMarketAeronaut copy() {
        return new NightMarketAeronaut(this);
    }
}
