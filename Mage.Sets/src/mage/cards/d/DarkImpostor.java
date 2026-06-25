package mage.cards.d;

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
import mage.filter.predicate.typed.ability.type.ActivatedAbilityPredicate;
import mage.filter.predicate.typed.mageObject.IMageObjectPredicate;
import mage.target.common.TargetCreaturePermanent;

import java.util.UUID;

/**
 * @author noxx
 */
public final class DarkImpostor extends CardImpl {

    private static final FilterTyped abilityFilter = new FilterTyped("activated abilities of all creature cards")
            .add(IMageObjectPredicate.getOSPPredicate(CardType.CREATURE.getPredicate()))
            .add(ActivatedAbilityPredicate.instance);

    public DarkImpostor(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{2}{B}");
        this.subtype.add(SubType.VAMPIRE);
        this.subtype.add(SubType.ASSASSIN);

        this.power = new MageInt(2);
        this.toughness = new MageInt(2);

        // {4}{B}{B}: Exile target creature and put a +1/+1 counter on Dark Impostor.
        Ability ability = new SimpleActivatedAbility(
                new ExileTargetEffect().setToSourceExileZone(true), new ManaCostsImpl<>("{4}{B}{B}")
        );
        ability.addEffect(new AddCountersSourceEffect(CounterType.P1P1.createInstance())
                .setText("and put a +1/+1 counter on {this}"));
        ability.addTarget(new TargetCreaturePermanent());
        this.addAbility(ability);

        // Dark Impostor has all activated abilities of all creature cards exiled with it.
        this.addAbility(new SimpleStaticAbility(new GainAbilitiesOfEffect()
                .fromSourceExiled()
                .setAbilityFilter(abilityFilter)
                .setText("{this} has all activated abilities of all creature cards exiled with it")
        ));
    }

    private DarkImpostor(final DarkImpostor card) {
        super(card);
    }

    @Override
    public DarkImpostor copy() {
        return new DarkImpostor(this);
    }
}

