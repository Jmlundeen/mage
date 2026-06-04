package mage.cards.c;

import mage.MageInt;
import mage.Mana;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.keyword.CrewAbility;
import mage.abilities.keyword.FlyingAbility;
import mage.abilities.keyword.VigilanceAbility;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.abilities.mana.conditional.SpendOrActivateManaCondition;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.filter.StaticTypedFilters;

import java.util.UUID;

/**
 * @author TheElk801
 */
public final class CargoShip extends CardImpl {

    public CargoShip(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT}, "{1}{U}");

        this.subtype.add(SubType.VEHICLE);
        this.power = new MageInt(2);
        this.toughness = new MageInt(3);

        // Flying
        this.addAbility(FlyingAbility.getInstance());

        // Vigilance
        this.addAbility(VigilanceAbility.getInstance());

        // {T}: Add {C}. Spend this mana only to cast an artifact spell or activate an ability of an artifact source.
        this.addAbility(new ComposedManaAbilityBuilder()
                .cost(new TapSourceCost())
                .addStatic(Mana.ColorlessMana(1))
                .condition(new SpendOrActivateManaCondition(StaticTypedFilters.AN_ARTIFACT))
                .ruleText("Add {C}. Spend this mana only to cast an artifact spell or activate an ability of an artifact source")
                .build()
        );

        // Crew 1
        this.addAbility(new CrewAbility(1));
    }

    private CargoShip(final CargoShip card) {
        super(card);
    }

    @Override
    public CargoShip copy() {
        return new CargoShip(this);
    }
}
