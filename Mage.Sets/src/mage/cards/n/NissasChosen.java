
package mage.cards.n;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.ReplacementEffectImpl;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.game.Game;
import mage.game.MoveCardsParameters;
import mage.game.events.GameEvent;
import mage.game.events.ZoneChangeEvent;
import mage.game.permanent.Permanent;
import mage.players.Player;

import java.util.UUID;

/**
 *
 * @author maurer.it_at_gmail.com
 */
public final class NissasChosen extends CardImpl {

    public NissasChosen(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId,setInfo,new CardType[]{CardType.CREATURE},"{G}{G}");
        this.subtype.add(SubType.ELF);
        this.subtype.add(SubType.WARRIOR);

        this.power = new MageInt(2);
        this.toughness = new MageInt(3);

        // If Nissa's Chosen would be put into a graveyard from the battlefield, put it on the bottom of its owner's library instead
        this.addAbility(new SimpleStaticAbility(new NissasChosenEffect()));
    }

    private NissasChosen(final NissasChosen card) {
        super(card);
    }

    @Override
    public NissasChosen copy() {
        return new NissasChosen(this);
    }
}

class NissasChosenEffect extends ReplacementEffectImpl {

    NissasChosenEffect() {
        super(Duration.WhileOnBattlefield, Outcome.Benefit);
        staticText = "If {this} would die, put it on the bottom of its owner's library instead";
    }

    private NissasChosenEffect(final NissasChosenEffect effect) {
        super(effect);
    }

    @Override
    public NissasChosenEffect copy() {
        return new NissasChosenEffect(this);
    }

    @Override
    public boolean checksEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.ZONE_CHANGE;
    }
    
    @Override
    public boolean applies(GameEvent event, Ability source, Game game) {
        if (event.getTargetId().equals(source.getSourceId())) {
            ZoneChangeEvent zEvent = (ZoneChangeEvent)event;
            if ( zEvent.isDiesEvent() ) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public boolean replaceEvent(GameEvent event, Ability source, Game game) {
        Permanent permanent = ((ZoneChangeEvent) event).getTarget();
        Player controller = game.getPlayer(source.getControllerId());
        if (permanent != null && controller != null) {
            MoveCardsParameters parameters = new MoveCardsParameters(permanent, Zone.LIBRARY)
                    .setToTopOfLibrary(false);
            controller.moveCards(parameters, source, game);
            return true;            
        }
        return false;
    }    
}
