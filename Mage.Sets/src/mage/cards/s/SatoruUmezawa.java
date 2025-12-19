package mage.cards.s;

import mage.MageInt;
import mage.MageItem;
import mage.abilities.Ability;
import mage.abilities.TriggeredAbilityImpl;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.effects.common.LookLibraryAndPickControllerEffect;
import mage.abilities.effects.common.continuous.generic.ContinuousEffectBuilder;
import mage.abilities.keyword.NinjutsuAbility;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.game.stack.StackAbility;
import mage.players.Player;

import java.util.List;
import java.util.UUID;

/**
 * @author TheElk801
 */
public final class SatoruUmezawa extends CardImpl {

    public SatoruUmezawa(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{1}{U}{B}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.NINJA);
        this.power = new MageInt(2);
        this.toughness = new MageInt(4);

        // Whenever you activate a ninjutsu ability, look at the top three cards of your library.
        // Put one of them into your hand and the rest on the bottom of your library in any order.
        // This ability triggers only once each turn.
        this.addAbility(new SatoruUmezawaTriggeredAbility());

        // Each creature card in your hand has ninjutsu {2}{U}{B}.
        this.addAbility(new SimpleStaticAbility(new ContinuousEffectBuilder(Duration.WhileOnBattlefield, Outcome.AddAbility)
                .setAffectedZones(Zone.HAND)
                .setCardFilter(StaticFilters.FILTER_CARD_CREATURE)
                .withGainedAbilities(new NinjutsuAbility("{2}{U}{B}"))
                .setText("each creature card in your hand has ninjutsu {2}{U}{B}")
        ));
    }

    private SatoruUmezawa(final SatoruUmezawa card) {
        super(card);
    }

    @Override
    public SatoruUmezawa copy() {
        return new SatoruUmezawa(this);
    }
}

class SatoruUmezawaTriggeredAbility extends TriggeredAbilityImpl {

    SatoruUmezawaTriggeredAbility() {
        super(Zone.BATTLEFIELD, new LookLibraryAndPickControllerEffect(3, 1, PutCards.HAND, PutCards.BOTTOM_ANY));
        this.setTriggersLimitEachTurn(1);
        setTriggerPhrase("Whenever you activate a ninjutsu ability, ");
    }

    private SatoruUmezawaTriggeredAbility(final SatoruUmezawaTriggeredAbility ability) {
        super(ability);
    }

    @Override
    public SatoruUmezawaTriggeredAbility copy() {
        return new SatoruUmezawaTriggeredAbility(this);
    }

    @Override
    public boolean checkEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.ACTIVATED_ABILITY;
    }

    @Override
    public boolean checkTrigger(GameEvent event, Game game) {
        if (!isControlledBy(event.getPlayerId())) {
            return false;
        }
        StackAbility stackAbility = (StackAbility) game.getStack().getStackObject(event.getTargetId());
        return stackAbility.getStackAbility() instanceof NinjutsuAbility;
    }
}
