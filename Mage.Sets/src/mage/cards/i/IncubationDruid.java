package mage.cards.i;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.dynamicvalue.DynamicValue;
import mage.abilities.effects.Effect;
import mage.abilities.keyword.AdaptAbility;
import mage.abilities.mana.AnyColorAmongManaAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.counters.CounterType;
import mage.filter.StaticTypedFilters;
import mage.game.Game;
import mage.game.permanent.Permanent;

import java.util.UUID;

/**
 * @author TheElk801
 */
public final class IncubationDruid extends CardImpl {

    public IncubationDruid(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{1}{G}");

        this.subtype.add(SubType.ELF);
        this.subtype.add(SubType.DRUID);
        this.power = new MageInt(0);
        this.toughness = new MageInt(2);

        // {T}: Add one mana of any type that a land you control could produce. If Incubation Druid has a +1/+1 counter on it, add three mana of that type instead.
        this.addAbility(AnyColorAmongManaAbility.builder(StaticTypedFilters.LAND_YOU_CONTROL)
                .onlyProducibleManaTypes(true)
                .amount(IncubationDruidValue.instance)
                .ruleText("Add one mana of any type that a land you control could produce. If {this} has a +1/+1 counter on it, add three mana of that type instead")
                .build()
        );

        // {3}{G}{G}: Adapt 3.
        this.addAbility(new AdaptAbility(3, "{3}{G}{G}"));
    }

    private IncubationDruid(final IncubationDruid card) {
        super(card);
    }

    @Override
    public IncubationDruid copy() {
        return new IncubationDruid(this);
    }
}

enum IncubationDruidValue implements DynamicValue {
    instance;

    @Override
    public int calculate(Game game, Ability sourceAbility, Effect effect) {
        if (game == null || sourceAbility == null) {
            return 1;
        }
        Permanent permanent = sourceAbility.getSourcePermanentOrLKI(game);
        return permanent != null && permanent.getCounters(game).getCount(CounterType.P1P1) > 0 ? 3 : 1;
    }

    @Override
    public DynamicValue copy() {
        return instance;
    }

    @Override
    public String getMessage() {
        return "";
    }
}
