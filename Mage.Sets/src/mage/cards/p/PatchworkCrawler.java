package mage.cards.p;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.common.ExileTargetEffect;
import mage.abilities.effects.common.continuous.layers.L6_Abilities.GainAbilitiesOfEffect;
import mage.abilities.effects.common.counter.AddCountersSourceEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.counters.CounterType;
import mage.filter.FilterTyped;
import mage.filter.StaticFilters;
import mage.filter.predicate.typed.ability.type.ActivatedAbilityPredicate;
import mage.filter.predicate.typed.mageObject.IMageObjectPredicate;
import mage.target.common.TargetCardInYourGraveyard;

import java.util.UUID;

/**
 * @author TheElk801
 */
public final class PatchworkCrawler extends CardImpl {

    private static final FilterTyped filter = new FilterTyped("activated abilities of a creature card")
            .add(ActivatedAbilityPredicate.instance)
            .add(IMageObjectPredicate.getOSPPredicate(CardType.CREATURE.getPredicate()));

    public PatchworkCrawler(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{1}{U}");

        this.subtype.add(SubType.ZOMBIE);
        this.subtype.add(SubType.HORROR);
        this.power = new MageInt(1);
        this.toughness = new MageInt(2);

        // {2}{U}: Exile target creature card from your graveyard and put a +1/+1 counter on Patchwork Crawler.
        Ability ability = new SimpleActivatedAbility(new ExileTargetEffect().setToSourceExileZone(true), new ManaCostsImpl<>("{2}{U}"));
        ability.addEffect(new AddCountersSourceEffect(CounterType.P1P1.createInstance()).concatBy("and"));
        ability.addTarget(new TargetCardInYourGraveyard(StaticFilters.FILTER_CARD_CREATURE_YOUR_GRAVEYARD));
        this.addAbility(ability);

        // Patchwork Crawler has all activated abilities of all creature cards exiled with it.
        this.addAbility(new SimpleStaticAbility(new GainAbilitiesOfEffect()
                .fromSourceExiled()
                .setAbilityFilter(filter)
                .setText("{this} has all activated abilities of all creature cards exiled with it")
        ));
    }

    private PatchworkCrawler(final PatchworkCrawler card) {
        super(card);
    }

    @Override
    public PatchworkCrawler copy() {
        return new PatchworkCrawler(this);
    }
}
