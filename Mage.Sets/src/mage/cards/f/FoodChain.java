package mage.cards.f;

import mage.abilities.Ability;
import mage.abilities.costs.Cost;
import mage.abilities.costs.common.ExileTargetCost;
import mage.abilities.dynamicvalue.DynamicValue;
import mage.abilities.effects.Effect;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.abilities.mana.conditional.CreatureCastManaCondition;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.ManaType;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.target.common.TargetControlledPermanent;

import java.util.Set;
import java.util.UUID;

/**
 * @author emerald000
 */
public final class FoodChain extends CardImpl {

    public FoodChain(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ENCHANTMENT}, "{2}{G}");

        // Exile a creature you control: Add X mana of any one color, where X is the exiled creature's converted mana cost plus one. Spend this mana only to cast creature spells.
        Ability ability = new ComposedManaAbilityBuilder()
                .cost(new ExileTargetCost(new TargetControlledPermanent(StaticFilters.FILTER_CONTROLLED_A_CREATURE)))
                .addDynamicChoice(FoodChainDynamicValue.instance, Set.of(ManaType.BLACK, ManaType.BLUE, ManaType.RED, ManaType.GREEN, ManaType.WHITE))
                .condition(new CreatureCastManaCondition())
                .ruleText("Add X mana of any one color, where X is the exiled creature's mana value plus one. Spend this mana only to cast creature spells")
                .build();
        this.addAbility(ability);
    }

    private FoodChain(final FoodChain card) {
        super(card);
    }

    @Override
    public FoodChain copy() {
        return new FoodChain(this);
    }
}

enum FoodChainDynamicValue implements DynamicValue {
    instance;

    @Override
    public int calculate(Game game, Ability sourceAbility, Effect effect) {
        Player controller = game.getPlayer(sourceAbility.getControllerId());
        if (controller != null) {
            int manaCostExiled = 0;
            for (Cost cost : sourceAbility.getCosts()) {
                if (cost.isPaid() && cost instanceof ExileTargetCost) {
                    for (Card card : ((ExileTargetCost) cost).getPermanents()) {
                        manaCostExiled += card.getManaValue();
                    }
                }
            }
            if (manaCostExiled == 0) {
                int cmc = -1;
                for (Permanent permanent : game.getBattlefield().getAllActivePermanents(controller.getId())) {
                    if (permanent.isCreature(game)) {
                        cmc = Math.max(cmc, permanent.getManaCost().manaValue());
                    }
                }
                return cmc + 1;
            } else {
                return manaCostExiled + 1;
            }
        }
        return 0;
    }

    @Override
    public DynamicValue copy() {
        return instance;
    }

    @Override
    public String getMessage() {
        return "X";
    }
}
