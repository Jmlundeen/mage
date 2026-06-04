package mage.cards.j;

import mage.Mana;
import mage.abilities.Ability;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.costs.mana.GenericManaCost;
import mage.abilities.effects.common.CreateTokenEffect;
import mage.abilities.mana.ColorlessManaAbility;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.abilities.mana.conditional.SpendOrActivateManaCondition;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.filter.FilterTyped;
import mage.game.permanent.token.AllyToken;

import java.util.UUID;

/**
 * @author TheElk801
 */
public final class JasmineDragonTeaShop extends CardImpl {

    static final FilterTyped filter = new FilterTyped("ally")
            .add(SubType.ALLY.getPredicate());

    public JasmineDragonTeaShop(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.LAND}, "");

        // {T}: Add {C}.
        this.addAbility(new ColorlessManaAbility());

        // {T}: Add one mana of any color. Spend this mana only to cast an Ally spell or activate an ability of an Ally source.
        this.addAbility(new ComposedManaAbilityBuilder()
                .cost(new TapSourceCost())
                .addStatic(Mana.AnyMana(1))
                .condition(new SpendOrActivateManaCondition(filter))
                .ruleText("Add one mana of any color. Spend this mana only to cast an Ally spell or activate an ability of an Ally source")
                .build()
        );

        // {5}, {T}: Create a 1/1 white Ally creature token.
        Ability ability = new SimpleActivatedAbility(new CreateTokenEffect(new AllyToken()), new GenericManaCost(5));
        ability.addCost(new TapSourceCost());
        this.addAbility(ability);
    }

    private JasmineDragonTeaShop(final JasmineDragonTeaShop card) {
        super(card);
    }

    @Override
    public JasmineDragonTeaShop copy() {
        return new JasmineDragonTeaShop(this);
    }
}
