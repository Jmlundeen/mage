package mage.cards.h;

import mage.MageInt;
import mage.MageItem;
import mage.abilities.Ability;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.effects.common.continuous.generic.ContinuousEffectBuilder;
import mage.abilities.effects.common.cost.CostModificationEffectImpl;
import mage.abilities.keyword.BlitzAbility;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.common.FilterCreatureCard;
import mage.filter.predicate.mageobject.ManaValuePredicate;
import mage.game.Game;
import mage.game.stack.Spell;
import mage.players.Player;
import mage.util.CardUtil;
import mage.watchers.common.CommanderPlaysCountWatcher;

import java.util.*;

/**
 * @author xenohedron
 */
public final class HenzieToolboxTorre extends CardImpl {

    private static final FilterCreatureCard filter = new FilterCreatureCard("creature spell you cast with mana value 4 or greater");
    static {
        filter.add(new ManaValuePredicate(ComparisonType.OR_GREATER, 4));
    }

    public HenzieToolboxTorre(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{B}{R}{G}");
        
        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.DEVIL);
        this.subtype.add(SubType.ROGUE);
        this.power = new MageInt(3);
        this.toughness = new MageInt(3);

        // Each creature you cast with mana value 4 or greater has blitz. The blitz cost is equal to its mana cost.
        this.addAbility(new SimpleStaticAbility(new ContinuousEffectBuilder(Duration.WhileOnBattlefield, Outcome.AddAbility)
                .setAffectedZones(Zone.LIBRARY, Zone.HAND, Zone.GRAVEYARD, Zone.EXILED, Zone.COMMAND, Zone.STACK)
                .setCardFilter(filter)
                .withGainedAbility((card, source, game) -> new BlitzAbility(card, card.getManaCost().getText()))
                .setText("Each creature spell you cast with mana value 4 or greater has blitz. The blitz cost is equal to its mana cost.")
        ));

        // Blitz costs you pay cost {1} less for each time you’ve cast your commander from the command zone this game.
        this.addAbility(new SimpleStaticAbility(new HenzieToolboxTorreBlitzDiscountEffect()));

    }

    private HenzieToolboxTorre(final HenzieToolboxTorre card) {
        super(card);
    }

    @Override
    public HenzieToolboxTorre copy() {
        return new HenzieToolboxTorre(this);
    }
}

class HenzieToolboxTorreBlitzDiscountEffect extends CostModificationEffectImpl {

    HenzieToolboxTorreBlitzDiscountEffect() {
        super(Duration.WhileOnBattlefield, Outcome.Benefit, CostModificationType.REDUCE_COST);
        this.staticText = "blitz costs you pay cost {1} less for each time you've cast your commander from the command zone this game";
    }

    private HenzieToolboxTorreBlitzDiscountEffect(final HenzieToolboxTorreBlitzDiscountEffect effect) {
        super(effect);
    }

    @Override
    public HenzieToolboxTorreBlitzDiscountEffect copy() {
        return new HenzieToolboxTorreBlitzDiscountEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source, Ability abilityToModify) {
        Player controller = game.getPlayer(abilityToModify.getControllerId());
        CommanderPlaysCountWatcher watcher = game.getState().getWatcher(CommanderPlaysCountWatcher.class);
        if (controller == null || watcher == null) {
            return false;
        }

        int playCount = game
                .getCommandersIds(controller, CommanderCardType.COMMANDER_OR_OATHBREAKER, false)
                .stream()
                .mapToInt(watcher::getPlaysCount)
                .sum();
        if (playCount == 0) {
            return false;
        }
        CardUtil.reduceCost(abilityToModify, playCount);
        return true;
    }

    @Override
    public boolean applies(Ability abilityToModify, Ability source, Game game) {
        if (!abilityToModify.isControlledBy(source.getControllerId())) {
            return false;
        }
        return abilityToModify instanceof BlitzAbility;
    }
}
