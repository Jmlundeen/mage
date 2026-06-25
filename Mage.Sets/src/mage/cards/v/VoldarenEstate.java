package mage.cards.v;

import mage.abilities.Ability;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.costs.CostAdjuster;
import mage.abilities.costs.common.PayLifeCost;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.costs.mana.GenericManaCost;
import mage.abilities.dynamicvalue.DynamicValue;
import mage.abilities.dynamicvalue.common.PermanentsOnBattlefieldCount;
import mage.abilities.effects.common.CreateTokenEffect;
import mage.abilities.hint.Hint;
import mage.abilities.hint.ValueHint;
import mage.abilities.mana.ColorlessManaAbility;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.abilities.mana.conditional.FilteredSpellManaCondition;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.filter.FilterTyped;
import mage.filter.common.FilterControlledPermanent;
import mage.filter.predicate.typed.mageObject.IMageObjectPredicate;
import mage.game.Game;
import mage.game.permanent.token.BloodToken;
import mage.util.CardUtil;

import java.util.UUID;

/**
 *
 * @author weirddan455
 */
public final class VoldarenEstate extends CardImpl {

    private static final FilterTyped filter = new FilterTyped("Vampire")
            .add(IMageObjectPredicate.getOSPPredicate(SubType.VAMPIRE.getPredicate()));

    public VoldarenEstate(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.LAND}, "");

        // {T}: Add {C}.
        this.addAbility(new ColorlessManaAbility());

        // {T}, Pay 1 life: Add one mana of any color. Spend this mana only to cast a Vampire spell.
        this.addAbility(ComposedManaAbilityBuilder.builder()
                .cost(new TapSourceCost())
                .cost(new PayLifeCost(1))
                .addAnyColor(1)
                .condition(new FilteredSpellManaCondition(filter))
                .ruleText("Add one mana of any color. Spend this mana only to cast a Vampire spell")
                .build()
        );

        // {5}, {T}: Create a Blood token. This ability costs {1} less to activate for each Vampire you control.
        Ability ability = new SimpleActivatedAbility(new CreateTokenEffect(new BloodToken()
            ).setText("Create a Blood token. This ability costs {1} less to activate for each Vampire you control"),
        new GenericManaCost(5));
        ability.addCost(new TapSourceCost());
        ability.setCostAdjuster(VoldarenEstateCostAdjuster.instance);
        ability.addHint(VoldarenEstateCostAdjuster.getHint());
        this.addAbility(ability);
    }

    private VoldarenEstate(final VoldarenEstate card) {
        super(card);
    }

    @Override
    public VoldarenEstate copy() {
        return new VoldarenEstate(this);
    }
}

enum VoldarenEstateCostAdjuster implements CostAdjuster {
    instance;

    private static final FilterControlledPermanent filter = new FilterControlledPermanent(SubType.VAMPIRE);
    private static final DynamicValue vampireCount = new PermanentsOnBattlefieldCount(filter);
    private static final Hint hint = new ValueHint("Vampires you control", vampireCount);

    public static Hint getHint() {
        return hint;
    }

    @Override
    public void reduceCost(Ability ability, Game game) {
        CardUtil.reduceCost(ability, vampireCount.calculate(game, ability, null));
    }
}
