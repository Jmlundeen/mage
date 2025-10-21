package mage.cards.c;

import mage.MageInt;
import mage.abilities.common.EntersBattlefieldAbility;
import mage.abilities.dynamicvalue.common.OpponentsLostLifeCount;
import mage.abilities.effects.common.continuous.replacement.EntersWithCountersEffect;
import mage.abilities.keyword.TrampleAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.counters.CounterType;

import java.util.UUID;

/**
 * @author LevelX2
 */
public final class CryptbornHorror extends CardImpl {

    public CryptbornHorror(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{1}{B/R}{B/R}");
        this.subtype.add(SubType.HORROR);
        this.color.setBlack(true);
        this.color.setRed(true);
        this.power = new MageInt(0);
        this.toughness = new MageInt(0);

        // Trample
        this.addAbility(TrampleAbility.getInstance());

        // Cryptborn Horror enters the battlefield with X +1/+1 counters on it, where X is the total life lost by your opponents this turn.
        this.addAbility(new EntersBattlefieldAbility(new EntersWithCountersEffect(CounterType.P1P1, OpponentsLostLifeCount.instance)
                .withXText()));
    }

    private CryptbornHorror(final CryptbornHorror card) {
        super(card);
    }

    @Override
    public CryptbornHorror copy() {
        return new CryptbornHorror(this);
    }
}
