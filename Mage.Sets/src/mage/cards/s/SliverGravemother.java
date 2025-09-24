package mage.cards.s;

import mage.MageInt;
import mage.MageItem;
import mage.abilities.Ability;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.costs.mana.GenericManaCost;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.effects.common.ruleModifying.LegendRuleDoesntApplyEffect;
import mage.abilities.keyword.EncoreAbility;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.FilterCard;
import mage.filter.FilterPermanent;
import mage.filter.common.FilterControlledPermanent;
import mage.filter.common.FilterCreatureCard;
import mage.game.Game;
import mage.players.Player;

import java.util.List;
import java.util.UUID;

/**
 * @author TheElk801
 */
public final class SliverGravemother extends CardImpl {

    private static final FilterPermanent filter = new FilterControlledPermanent(SubType.SLIVER, "Slivers you control");

    public SliverGravemother(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{W}{U}{B}{R}{G}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.SLIVER);
        this.power = new MageInt(6);
        this.toughness = new MageInt(6);

        // The "legend rule" doesn't apply to Slivers you control.
        this.addAbility(new SimpleStaticAbility(new LegendRuleDoesntApplyEffect(filter)));

        // Each Sliver creature card in your graveyard has encore {X}, where X is its mana value.
        this.addAbility(new SimpleStaticAbility(new SliverGravemotherEffect()));

        // Encore {5}
        this.addAbility(new EncoreAbility(new ManaCostsImpl<>("{5}")));
    }

    private SliverGravemother(final SliverGravemother card) {
        super(card);
    }

    @Override
    public SliverGravemother copy() {
        return new SliverGravemother(this);
    }
}

class SliverGravemotherEffect extends ContinuousEffectImpl {

    private static final FilterCard filter = new FilterCreatureCard();

    static {
        filter.add(SubType.SLIVER.getPredicate());
    }

    public SliverGravemotherEffect() {
        super(Duration.WhileOnBattlefield, Layer.AbilityAddingRemovingEffects_6, SubLayer.NA, Outcome.AddAbility);
        this.staticText = "each Sliver creature card in your graveyard has encore {X}, where X is its mana value";
    }

    private SliverGravemotherEffect(final SliverGravemotherEffect effect) {
        super(effect);
    }

    @Override
    public SliverGravemotherEffect copy() {
        return new SliverGravemotherEffect(this);
    }

    @Override
    public void applyToObjects(Layer layer, SubLayer sublayer, Ability source, Game game, List<MageItem> affectedObjects) {
        for (MageItem object : affectedObjects) {
            Card card = (Card) object;
            card.getAbilities().add(new EncoreAbility(new GenericManaCost(card.getManaValue())));
        }
    }

    @Override
    public boolean queryAffectedObjects(Layer layer, Ability source, Game game, List<MageItem> affectedObjects) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        affectedObjects.addAll(controller.getGraveyard().getCards(filter, game));
        return !affectedObjects.isEmpty();
    }
}
