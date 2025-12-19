package mage.cards.c;

import mage.MageObject;
import mage.abilities.Ability;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.costs.Cost;
import mage.abilities.costs.CostImpl;
import mage.abilities.costs.EarlyTargetCost;
import mage.abilities.dynamicvalue.DynamicValue;
import mage.abilities.dynamicvalue.common.PermanentsOnBattlefieldCount;
import mage.abilities.effects.common.continuous.generic.ContinuousEffectBuilder;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.choices.Choice;
import mage.choices.ChoiceCreatureType;
import mage.constants.*;
import mage.filter.common.FilterCreaturePermanent;
import mage.filter.predicate.mageobject.ChosenCreatureTypePredicate;
import mage.game.Game;
import mage.players.Player;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author jeffwadsworth, JayDi85
 */
public final class CallerOfTheHunt extends CardImpl {
    private static final FilterCreaturePermanent filter = new FilterCreaturePermanent("creatures of the chosen type");
    private static final DynamicValue xValue = new PermanentsOnBattlefieldCount(filter);

    static {
        filter.add(ChosenCreatureTypePredicate.TRUE);
    }

    public CallerOfTheHunt(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{2}{G}");

        this.subtype.add(SubType.HUMAN);

        // As an additional cost to cast Caller of the Hunt, choose a creature type.
        this.getSpellAbility().addCost(new CallerOfTheHuntCost());

        // Caller of the Hunt's power and toughness are each equal to the number of creatures of the chosen type on the battlefield.
        this.addAbility(new SimpleStaticAbility(Zone.ALL, new ContinuousEffectBuilder(Duration.EndOfGame, Outcome.BoostCreature, ContinuousAffected.SOURCE)
                .withSetPower(xValue)
                .withSetToughness(xValue)
                .setText("{this}'s power and toughness are each equal to the number of creatures of the chosen type on the battlefield")
        ));

    }

    private CallerOfTheHunt(final CallerOfTheHunt card) {
        super(card);
    }

    @Override
    public CallerOfTheHunt copy() {
        return new CallerOfTheHunt(this);
    }
}

class CallerOfTheHuntCost extends CostImpl implements EarlyTargetCost {

    CallerOfTheHuntCost() {
        super();
    }

    @Override
    public String getText() {
        return "as an additional cost to cast this spell, choose a creature type";
    }

    private CallerOfTheHuntCost(final CallerOfTheHuntCost cost) {
        super(cost);
    }

    @Override
    public boolean canPay(Ability ability, Ability source, UUID controllerId, Game game) {
        return true;
    }

    @Override
    public boolean pay(Ability ability, Game game, Ability source, UUID controllerId, boolean noMana, Cost costToPay) {
        MageObject sourceObject = game.getObject(source.getSourceId());
        Player controller = game.getPlayer(controllerId);
        if (sourceObject == null || controller == null) {
            return paid;
        }
        SubType chosenType = (SubType) game.getState().getValue(source.getSourceId() + "_type");
        if (chosenType == null) {
            return paid;
        }
        return paid = true;
    }

    @Override
    public CallerOfTheHuntCost copy() {
        return new CallerOfTheHuntCost(this);
    }

    @Override
    public void chooseTarget(Game game, Ability source, Player controller) {
        MageObject sourceObject = game.getObject(source.getSourceId());
        if (sourceObject == null) {
            return;
        }
        SubType chosenType = (SubType) game.getState().getValue(source.getSourceId() + "_type");
        if (chosenType != null) {
            return;
        }

        // choose creature type
        if (controller.isComputer()) {
            // AI hint - find best creature type with max permanents, all creature type supports too
            Map<SubType, Integer> usedSubTypeStats = new HashMap<>();
            game.getBattlefield().getActivePermanents(source.getControllerId(), game)
                    .stream()
                    .map(permanent -> permanent.getSubtype(game))
                    .flatMap(Collection::stream)
                    .distinct()
                    .forEach(subType -> {
                        FilterCreaturePermanent filter = new FilterCreaturePermanent();
                        filter.add(subType.getPredicate());
                        int amount = new PermanentsOnBattlefieldCount(filter).calculate(game, source, null);
                        usedSubTypeStats.put(subType, amount);
                    });
            int maxAmount = 0;
            SubType maxSubType = null;
            for (Map.Entry<SubType, Integer> entry : usedSubTypeStats.entrySet()) {
                if (entry.getValue() > maxAmount) {
                    maxSubType = entry.getKey();
                    maxAmount = entry.getValue();
                }
            }
            game.getState().setValue(source.getSourceId() + "_type", maxSubType);
        } else {
            Choice typeChoice = new ChoiceCreatureType(game, source);
            if (controller.choose(Outcome.Benefit, typeChoice, game)) {
                if (!game.isSimulation()) {
                    game.informPlayers(sourceObject.getName() + ": "
                            + controller.getLogName() + " has chosen " + typeChoice.getChoiceKey());
                }
                game.getState().setValue(source.getSourceId()
                        + "_type", SubType.byDescription(typeChoice.getChoiceKey()));
            }
        }
    }
}