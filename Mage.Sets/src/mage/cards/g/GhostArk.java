package mage.cards.g;

import mage.MageInt;
import mage.MageItem;
import mage.MageObjectReference;
import mage.abilities.Ability;
import mage.abilities.TriggeredAbilityImpl;
import mage.abilities.costs.mana.GenericManaCost;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.effects.common.continuous.generic.ContinuousEffectBuilder;
import mage.abilities.keyword.CrewAbility;
import mage.abilities.keyword.FlyingAbility;
import mage.abilities.keyword.UnearthAbility;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.FilterCard;
import mage.filter.StaticFilters;
import mage.filter.common.FilterArtifactCard;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.players.Player;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author TheElk801
 */
public final class GhostArk extends CardImpl {

    public GhostArk(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT}, "{4}");

        this.subtype.add(SubType.VEHICLE);
        this.power = new MageInt(3);
        this.toughness = new MageInt(3);

        // Flying
        this.addAbility(FlyingAbility.getInstance());

        // Repair Barge -- Whenever Ghost Ark becomes crewed, each artifact creature card in your graveyard gains unearth {3} until end of turn.
        this.addAbility(new GhostArkTriggeredAbility());

        // Crew 2
        this.addAbility(new CrewAbility(2));
    }

    private GhostArk(final GhostArk card) {
        super(card);
    }

    @Override
    public GhostArk copy() {
        return new GhostArk(this);
    }
}

class GhostArkTriggeredAbility extends TriggeredAbilityImpl {

    private static final FilterCard filter = new FilterArtifactCard();

    static {
        filter.add(CardType.CREATURE.getPredicate());
    }

    GhostArkTriggeredAbility() {
        super(Zone.BATTLEFIELD, new ContinuousEffectBuilder(Duration.EndOfTurn, Outcome.AddAbility)
                .setAffectedZones(Zone.GRAVEYARD)
                .setCardFilter(filter)
                .withGainedAbilities(new UnearthAbility(new GenericManaCost(3)))
                .setText("each artifact creature card in your graveyard gains unearth {3} until end of turn")
        );
        setTriggerPhrase("Whenever {this} becomes crewed, ");
        this.withFlavorWord("Repair Barge");
    }

    private GhostArkTriggeredAbility(final GhostArkTriggeredAbility ability) {
        super(ability);
    }

    @Override
    public GhostArkTriggeredAbility copy() {
        return new GhostArkTriggeredAbility(this);
    }

    @Override
    public boolean checkEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.VEHICLE_CREWED;
    }

    @Override
    public boolean checkTrigger(GameEvent event, Game game) {
        return event.getTargetId().equals(getSourceId());
    }
}
