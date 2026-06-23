package mage.cards.i;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.condition.common.FerociousCondition;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.dynamicvalue.DynamicValue;
import mage.abilities.effects.Effect;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.game.Game;

import java.util.UUID;

/**
 *
 * @author LevelX2
 */
public final class IlysianCaryatid extends CardImpl {

    public IlysianCaryatid(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{1}{G}");

        this.subtype.add(SubType.PLANT);
        this.power = new MageInt(1);
        this.toughness = new MageInt(1);

        // {T}: Add one mana of any color. If you control a creature with power 4 or greater, add two mana of any one color instead.
        this.addAbility(ComposedManaAbilityBuilder.builder()
                        .cost(new TapSourceCost())
                        .addDynamicAnyColor(IlysianCaryatidValue.instance)
                .ruleText("Add one mana of any color. If you control a creature with power 4 or greater, add two mana of any one color instead")
                .build()
        );
    }

    private IlysianCaryatid(final IlysianCaryatid card) {
        super(card);
    }

    @Override
    public IlysianCaryatid copy() {
        return new IlysianCaryatid(this);
    }
}

enum IlysianCaryatidValue implements DynamicValue {
    instance;

    @Override
    public int calculate(Game game, Ability sourceAbility, Effect effect) {
        if (game == null || sourceAbility == null) {
            return 1;
        }
        return FerociousCondition.instance.apply(game, sourceAbility) ? 2 : 1;
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