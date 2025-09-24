
package mage.cards.r;

import mage.MageItem;
import mage.abilities.Ability;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.condition.common.SourceIsSpellCondition;
import mage.abilities.costs.AlternativeCostSourceAbility;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.FilterCard;
import mage.game.Game;
import mage.players.Player;

import java.util.List;
import java.util.UUID;

/**
 *
 * @author BetaSteward
 */
public final class RooftopStorm extends CardImpl {

    public RooftopStorm(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ENCHANTMENT}, "{5}{U}");

        // You may pay {0} rather than pay the mana cost for Zombie creature spells you cast.
        this.addAbility(new SimpleStaticAbility(new RooftopStormRuleEffect()));

    }

    private RooftopStorm(final RooftopStorm card) {
        super(card);
    }

    @Override
    public RooftopStorm copy() {
        return new RooftopStorm(this);
    }
}

class RooftopStormRuleEffect extends ContinuousEffectImpl {

    private static final FilterCard filter = new FilterCard("Zombie creature spells");

    static {
        filter.add(SubType.ZOMBIE.getPredicate());
        filter.add(CardType.CREATURE.getPredicate());
    }

    private final AlternativeCostSourceAbility alternativeCastingCostAbility
            = new AlternativeCostSourceAbility(new ManaCostsImpl<>("{0}"), SourceIsSpellCondition.instance, null, filter, true);

    public RooftopStormRuleEffect() {
        super(Duration.WhileOnBattlefield, Layer.RulesEffects, SubLayer.NA, Outcome.Detriment);
        staticText = "You may pay {0} rather than pay the mana cost for Zombie creature spells you cast";
    }

    private RooftopStormRuleEffect(final RooftopStormRuleEffect effect) {
        super(effect);
    }

    @Override
    public RooftopStormRuleEffect copy() {
        return new RooftopStormRuleEffect(this);
    }

    @Override
    public void init(Ability source, Game game, UUID activePlayerId) {
        super.init(source, game, activePlayerId);
        alternativeCastingCostAbility.setSourceId(source.getSourceId());
    }

    @Override
    public void applyToObjects(Layer layer, SubLayer sublayer, Ability source, Game game, List<MageItem> affectedObjects) {
        for (MageItem object : affectedObjects) {
            ((Player) object).getAlternativeSourceCosts().remove(alternativeCastingCostAbility);
        }
    }

    @Override
    public boolean queryAffectedObjects(Layer layer, Ability source, Game game, List<MageItem> affectedObjects) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller != null) {
            affectedObjects.add(controller);
            return true;
        } else {
            return false;
        }
    }
}
