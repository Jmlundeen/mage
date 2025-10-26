
package mage.cards.r;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.costs.common.RemoveCountersSourceCost;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.dynamicvalue.common.CardsInAllHandsCount;
import mage.abilities.effects.common.continuous.replacement.EntersWithCountersEffect;
import mage.abilities.effects.common.search.SearchLibraryPutInHandEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.counters.CounterType;
import mage.filter.common.FilterLandCard;
import mage.target.common.TargetCardInLibrary;

import java.util.UUID;

/**
 *
 * @author emerald000
 */
public final class RealmSeekers extends CardImpl {

    public RealmSeekers(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId,setInfo,new CardType[]{CardType.CREATURE},"{4}{G}{G}");
        this.subtype.add(SubType.ELF);
        this.subtype.add(SubType.SCOUT);
        this.power = new MageInt(0);
        this.toughness = new MageInt(0);

        // Realm Seekers enters the battlefield with X +1/+1 counters on it, where X is the total number of cards in all players' hands.
        this.addAbility(new SimpleStaticAbility(new EntersWithCountersEffect(CounterType.P1P1, CardsInAllHandsCount.instance)
                        .withXText()
        ));
        
        // {2}{G}, Remove a +1/+1 counter from Realm Seekers: Search your library for a land card, reveal it, put it into your hand, then shuffle your library.
        Ability ability = new SimpleActivatedAbility(new SearchLibraryPutInHandEffect(new TargetCardInLibrary(new FilterLandCard()), true), new ManaCostsImpl<>("{2}{G}"));
        ability.addCost(new RemoveCountersSourceCost(CounterType.P1P1.createInstance()));
        this.addAbility(ability);
    }

    private RealmSeekers(final RealmSeekers card) {
        super(card);
    }

    @Override
    public RealmSeekers copy() {
        return new RealmSeekers(this);
    }
}
