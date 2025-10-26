package mage.cards.r;

import mage.MageInt;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.common.SpellCastControllerTriggeredAbility;
import mage.abilities.dynamicvalue.AdditiveDynamicValue;
import mage.abilities.dynamicvalue.DynamicValue;
import mage.abilities.dynamicvalue.common.ObjectManaValue;
import mage.abilities.dynamicvalue.common.StaticValue;
import mage.abilities.effects.common.continuous.generic.ContinuousEffectBuilder;
import mage.abilities.effects.common.continuous.replacement.EntersWithCountersEffect;
import mage.abilities.keyword.HasteAbility;
import mage.abilities.mana.GreenManaAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.counters.CounterType;
import mage.filter.FilterPermanent;
import mage.filter.FilterSpell;
import mage.filter.common.FilterControlledCreaturePermanent;
import mage.filter.predicate.Predicate;
import mage.filter.predicate.mageobject.ManaValuePredicate;
import mage.game.Game;
import mage.game.permanent.Permanent;

import java.util.UUID;

/**
 *
 * @author @stwalsh4118
 */
public final class RunadiBehemothCaller extends CardImpl {

    private static final FilterPermanent filter = new FilterControlledCreaturePermanent("creatures you control with three or more +1/+1 counters on them");

    private static final FilterSpell filterSpell = new FilterSpell("a creature spell with mana value 5 or greater");
    private static final DynamicValue manaValue = new AdditiveDynamicValue(
            ObjectManaValue.SPELL,
            StaticValue.get(-4)
    );
    static {
        filter.add(RunadiBehemothCallerPredicate.instance);
        filterSpell.add(CardType.CREATURE.getPredicate());
        filterSpell.add(new ManaValuePredicate(ComparisonType.OR_GREATER, 5));
    }

    public RunadiBehemothCaller(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{2}{G}");
        
        this.addSuperType(SuperType.LEGENDARY);
        this.subtype.add(SubType.CAT);
        this.subtype.add(SubType.SHAMAN);
        this.power = new MageInt(1);
        this.toughness = new MageInt(3);

        // Whenever you cast a creature spell with mana value 5 or greater, that creature enters the battlefield with X additional +1/+1 counters on it, where X is its mana value minus 4.
        this.addAbility(new SpellCastControllerTriggeredAbility(
                new EntersWithCountersEffect(ContinuousAffected.STATIC_OR_DYNAMIC, CounterType.P1P1, manaValue)
                        .setText("that creature enters with X additional +1/+1 counters on it, where X is its mana value minus 4"),
                filterSpell,
                false, SetTargetPointer.CARD));

        // Creatures you control with three or more +1/+1 counters on them have haste.
        this.addAbility(new SimpleStaticAbility(new ContinuousEffectBuilder(Outcome.AddAbility, filter)
                .withGainedAbilities(HasteAbility.getInstance())
                .setText("creatures you control with three or more +1/+1 counters on them have haste")
        ));
        
        // {T}: Add {G}.
        this.addAbility(new GreenManaAbility());

    }

    private RunadiBehemothCaller(final RunadiBehemothCaller card) {
        super(card);
    }

    @Override
    public RunadiBehemothCaller copy() {
        return new RunadiBehemothCaller(this);
    }
}

enum RunadiBehemothCallerPredicate implements Predicate<Permanent> {
    instance;

    @Override
    public boolean apply(Permanent input, Game game) {
        return input.getCounters(game).getCount(CounterType.P1P1) >= 3;
    }

    @Override
    public String toString() {
        return "with three or more +1/+1 counters on them";
    }
}
