package mage.cards.g;

import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.effects.keyword.SurveilEffect;
import mage.abilities.mana.ColorlessManaAbility;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.abilities.mana.conditional.SpendOrActivateManaCondition;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.filter.FilterTyped;
import mage.filter.predicate.typed.LogicalPredicate;

import java.util.UUID;

/**
 * @author TheElk801
 */
public final class GallifreyCouncilChamber extends CardImpl {

    static final FilterTyped filter = new FilterTyped("time lord or alien")
            .add(LogicalPredicate.or(
                    SubType.TIME_LORD.getPredicate(),
                    SubType.ALIEN.getPredicate()
            ));

    public GallifreyCouncilChamber(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.LAND}, "");

        this.supertype.add(SuperType.LEGENDARY);

        // When Gallifrey Council Chamber enters the battlefield, surveil 1.
        this.addAbility(new EntersBattlefieldTriggeredAbility(new SurveilEffect(1)));

        // {T}: Add {C}.
        this.addAbility(new ColorlessManaAbility());

        // {T}: Add one mana of any color. Spend this mana only to cast a Time Lord or Alien spell or activate an ability of a Time Lord or Alien.
        this.addAbility(new ComposedManaAbilityBuilder()
                .cost(new TapSourceCost())
                .addAnyColor(1)
                .condition(new SpendOrActivateManaCondition(filter))
                .ruleText("Add one mana of any color. Spend this mana only to cast a Time Lord or Alien spell or activate an ability of a Time Lord or Alien")
                .build()
        );
    }

    private GallifreyCouncilChamber(final GallifreyCouncilChamber card) {
        super(card);
    }

    @Override
    public GallifreyCouncilChamber copy() {
        return new GallifreyCouncilChamber(this);
    }
}
