package mage.cards.k;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.dynamicvalue.common.StaticValue;
import mage.abilities.effects.Effect;
import mage.abilities.effects.common.continuous.GainAbilityControlledEffect;
import mage.abilities.effects.common.counter.AddCountersAllEffect;
import mage.abilities.keyword.ProtectionAbility;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.abilities.mana.providers.ManaTypeProvider;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.counters.CounterType;
import mage.filter.FilterPermanent;
import mage.filter.StaticFilters;
import mage.filter.common.FilterControlledCreaturePermanent;
import mage.game.Game;
import mage.game.permanent.Permanent;

import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

/**
 * @author weirddan455
 */
public final class KatildaDawnhartPrime extends CardImpl {

    private static final FilterPermanent filter = new FilterPermanent(SubType.WEREWOLF, "Werewolves");
    private static final FilterPermanent filter2 = new FilterControlledCreaturePermanent(SubType.HUMAN, "Human creatures");

    public KatildaDawnhartPrime(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{G}{W}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.WARLOCK);
        this.power = new MageInt(1);
        this.toughness = new MageInt(1);

        // Protection from Werewolves
        this.addAbility(new ProtectionAbility(filter));

        // Human creatures you control have "{T}: Add one mana of any of this creature's colors."
        this.addAbility(new SimpleStaticAbility(new GainAbilityControlledEffect(
                new ComposedManaAbilityBuilder()
                        .addDynamicChoice(StaticValue.get(1), KatildaDawnhartPrimeManaTypes.instance)
                        .cost(new TapSourceCost())
                        .ruleText("Add one mana of any of this creature's colors")
                        .build(),
                Duration.WhileOnBattlefield, filter2))
        );

        // {4}{G}{W}, {T}: Put a +1/+1 counter on each creature you control.
        Ability ability = new SimpleActivatedAbility(new AddCountersAllEffect(
                CounterType.P1P1.createInstance(), StaticFilters.FILTER_CONTROLLED_CREATURE
        ), new ManaCostsImpl<>("{4}{G}{W}"));
        ability.addCost(new TapSourceCost());
        this.addAbility(ability);
    }

    private KatildaDawnhartPrime(final KatildaDawnhartPrime card) {
        super(card);
    }

    @Override
    public KatildaDawnhartPrime copy() {
        return new KatildaDawnhartPrime(this);
    }
}

enum KatildaDawnhartPrimeManaTypes implements ManaTypeProvider {
    instance;

    @Override
    public Set<ManaType> getManaTypes(Game game, Ability source, Effect effect) {
        if (game == null || source == null) {
            return EnumSet.noneOf(ManaType.class);
        }
        Permanent permanent = source.getSourcePermanentIfItStillExists(game);
        return permanent == null
                ? EnumSet.noneOf(ManaType.class)
                : ManaType.getManaTypesFromObjectColor(permanent.getColor(game));
    }
}
