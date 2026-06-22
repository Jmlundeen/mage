package mage.cards.s;

import com.google.common.base.Functions;
import mage.MageObject;
import mage.abilities.Ability;
import mage.abilities.ActivatedAbilityImpl;
import mage.abilities.condition.Condition;
import mage.abilities.mana.AnyColorManaAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SuperType;
import mage.filter.StaticFilters;
import mage.game.Game;

import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author TheElk801
 */
public final class SceptreOfEternalGlory extends CardImpl {

    public SceptreOfEternalGlory(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT}, "{4}");

        this.supertype.add(SuperType.LEGENDARY);

        // {T}: Add one mana of any color.
        this.addAbility(new AnyColorManaAbility());

        // {T}: Add three mana of any one color. Activate only if you control three or more lands with the same name.
        ActivatedAbilityImpl ability = new AnyColorManaAbility(3)
                .setCondition(SceptreOfEternalGloryCondition.instance);
        ability.appendToRule(" Activate only if you control three or more lands with the same name.");
        this.addAbility(ability);
    }

    private SceptreOfEternalGlory(final SceptreOfEternalGlory card) {
        super(card);
    }

    @Override
    public SceptreOfEternalGlory copy() {
        return new SceptreOfEternalGlory(this);
    }
}

enum SceptreOfEternalGloryCondition implements Condition {
    instance;

    @Override
    public boolean apply(Game game, Ability source) {
        return game
                .getBattlefield()
                .getActivePermanents(
                        StaticFilters.FILTER_CONTROLLED_PERMANENT_LAND,
                        source.getControllerId(), source, game
                )
                .stream()
                .map(MageObject::getName)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toMap(
                        Functions.identity(),
                        x -> 1, Integer::sum
                ))
                .values()
                .stream()
                .anyMatch(x -> x >= 3);
    }

    @Override
    public String toString() {
        return "you control three or more lands with the same name";
    }
}
