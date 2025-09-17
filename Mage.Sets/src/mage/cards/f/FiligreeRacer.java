package mage.cards.f;

import mage.MageInt;
import mage.MageItem;
import mage.abilities.Ability;
import mage.abilities.common.AttacksTriggeredAbility;
import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.common.delayed.ReflexiveTriggeredAbility;
import mage.abilities.costs.common.PayEnergyCost;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.effects.common.DoWhenCostPaid;
import mage.abilities.effects.common.counter.GetEnergyCountersControllerEffect;
import mage.abilities.keyword.CrewAbility;
import mage.abilities.keyword.JumpStartAbility;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.FilterCard;
import mage.filter.predicate.Predicates;
import mage.game.Game;
import mage.target.common.TargetCardInYourGraveyard;

import java.util.List;
import java.util.UUID;

/**
 *
 * @author grimreap124
 */
public final class FiligreeRacer extends CardImpl {

    private static final FilterCard filter = new FilterCard("instant or sorcery card in your graveyard");

    static {
        filter.add(Predicates.or(
                CardType.INSTANT.getPredicate(),
                CardType.SORCERY.getPredicate()));
    }

    public FiligreeRacer(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT}, "{3}{R}");
        
        this.subtype.add(SubType.VEHICLE);
        this.power = new MageInt(5);
        this.toughness = new MageInt(5);

        // When Filigree Racer enters the battlefield, you get {E}{E}{E}{E}.
        this.addAbility(new EntersBattlefieldTriggeredAbility(new GetEnergyCountersControllerEffect(4)));
        // Whenever Filigree Racer attacks, you may pay {E}{E}. When you do, target instant or sorcery card in your graveyard gains jump-start until end of turn.
        ReflexiveTriggeredAbility reflexiveTriggeredAbility = new ReflexiveTriggeredAbility(new FiligreeRacerEffect(), false );
        reflexiveTriggeredAbility.addTarget(new TargetCardInYourGraveyard(filter));
        Ability ability = new AttacksTriggeredAbility(new DoWhenCostPaid(reflexiveTriggeredAbility, new PayEnergyCost(2), "You may pay {E}{E}."), false);
        this.addAbility(ability);
        // Crew 1
        this.addAbility(new CrewAbility(1));

    }

    private FiligreeRacer(final FiligreeRacer card) {
        super(card);
    }

    @Override
    public FiligreeRacer copy() {
        return new FiligreeRacer(this);
    }
}

class FiligreeRacerEffect extends ContinuousEffectImpl {

    FiligreeRacerEffect() {
        super(Duration.EndOfTurn, Layer.AbilityAddingRemovingEffects_6, SubLayer.NA, Outcome.AddAbility);
        staticText = "target instant or sorcery card in your graveyard gains jump-start until end of turn.";
    }

    private FiligreeRacerEffect(final FiligreeRacerEffect effect) {
        super(effect);
    }

    @Override
    public void applyToObjects(Layer layer, SubLayer sublayer, Ability source, Game game, List<MageItem> affectedObjects) {
        for (MageItem object : affectedObjects) {
            Card card = (Card) object;
            JumpStartAbility ability = new JumpStartAbility(card);
            ability.setSourceId(object.getId());
            ability.setControllerId((card).getOwnerId());
            game.getState().addOtherAbility(card, ability);
        }
    }

    @Override
    public boolean queryAffectedObjects(Layer layer, Ability source, Game game, List<MageItem> affectedObjects) {
        Card card = game.getCard(getTargetPointer().getFirst(game, source));
        if (card == null) {
            return false;
        }
        affectedObjects.add(card);
        return true;
    }

    @Override
    public FiligreeRacerEffect copy() {
        return new FiligreeRacerEffect(this);
    }
}