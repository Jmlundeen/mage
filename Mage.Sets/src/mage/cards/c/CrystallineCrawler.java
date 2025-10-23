package mage.cards.c;

import mage.MageInt;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.costs.common.RemoveCountersSourceCost;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.dynamicvalue.common.ColorsOfManaSpentToCastCount;
import mage.abilities.dynamicvalue.common.CountersSourceCount;
import mage.abilities.effects.common.continuous.replacement.EntersWithCountersEffect;
import mage.abilities.effects.common.counter.AddCountersSourceEffect;
import mage.abilities.mana.AnyColorManaAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.counters.CounterType;

import java.util.UUID;

/**
 *
 * @author fireshoes
 */
public final class CrystallineCrawler extends CardImpl {

    public CrystallineCrawler(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT, CardType.CREATURE}, "{4}");

        this.subtype.add(SubType.CONSTRUCT);
        this.power = new MageInt(1);
        this.toughness = new MageInt(1);

        // Converge - Crystalline Crawler enters the battlefield with a +1/+1 counter on it for each color of mana spent to cast it.
        this.addAbility(new SimpleStaticAbility(new EntersWithCountersEffect(CounterType.P1P1, ColorsOfManaSpentToCastCount.getInstance())
                        .setText("{this} enters with a +1/+1 counter on it for each color of mana spent to cast it"))
                .withFlavorWord("Converge"));

        // Remove a +1/+1 counter from Crystalline Crawler: Add one mana of any color.
        this.addAbility(new AnyColorManaAbility(new RemoveCountersSourceCost(CounterType.P1P1.createInstance(1)),
                new CountersSourceCount(CounterType.P1P1), false));

        // {T}: Put a +1/+1 counter on Crystalline Crawler.
        this.addAbility(new SimpleActivatedAbility(new AddCountersSourceEffect(CounterType.P1P1.createInstance(1)), new TapSourceCost()));
    }

    private CrystallineCrawler(final CrystallineCrawler card) {
        super(card);
    }

    @Override
    public CrystallineCrawler copy() {
        return new CrystallineCrawler(this);
    }
}
