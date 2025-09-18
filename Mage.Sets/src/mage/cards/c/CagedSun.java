package mage.cards.c;

import mage.Mana;
import mage.ObjectColor;
import mage.abilities.Ability;
import mage.abilities.common.AsEntersBattlefieldAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.common.ChooseColorEffect;
import mage.abilities.effects.common.continuous.BoostAllOfChosenColorEffect;
import mage.abilities.effects.mana.ManaEffect;
import mage.abilities.mana.TriggeredManaAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.game.permanent.Permanent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author BetaSteward
 */
public final class CagedSun extends CardImpl {

    public CagedSun(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT}, "{6}");

        // As Caged Sun enters the battlefield, choose a color.
        this.addAbility(new AsEntersBattlefieldAbility(new ChooseColorEffect(Outcome.Benefit)));

        // Creatures you control of the chosen color get +1/+1.
        this.addAbility(new SimpleStaticAbility(new BoostAllOfChosenColorEffect(1, 1, Duration.WhileOnBattlefield, false)));

        // Whenever a land's ability adds one or more mana of the chosen color, add one additional mana of that color.
        this.addAbility(new CagedSunTriggeredAbility());
    }

    private CagedSun(final CagedSun card) {
        super(card);
    }

    @Override
    public CagedSun copy() {
        return new CagedSun(this);
    }
}

class CagedSunTriggeredAbility extends TriggeredManaAbility {

    private static final String staticText = "Whenever a land's ability causes you to add one or more mana of the chosen color, add one additional mana of that color.";

    public CagedSunTriggeredAbility() {
        super(Zone.BATTLEFIELD, new CagedSunEffect());
    }

    private CagedSunTriggeredAbility(final CagedSunTriggeredAbility ability) {
        super(ability);
    }

    @Override
    public boolean checkEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.MANA_ADDED;
    }

    @Override
    public boolean checkTrigger(GameEvent event, Game game) {
        if (event.getPlayerId().equals(controllerId)) {
            Permanent permanent = game.getPermanentOrLKIBattlefield(event.getSourceId());
            if (permanent != null && permanent.isLand(game)) {
                ObjectColor color = (ObjectColor) game.getState().getValue(this.sourceId + "_color");
                return color != null && event.getData().contains(color.toString());
            }
        }
        return false;
    }

    @Override
    public CagedSunTriggeredAbility copy() {
        return new CagedSunTriggeredAbility(this);
    }

    @Override
    public String getRule() {
        return staticText;
    }
}

class CagedSunEffect extends ManaEffect {

    CagedSunEffect() {
        super();
    }

    private CagedSunEffect(final CagedSunEffect effect) {
        super(effect);
    }

    @Override
    public List<Mana> getNetMana(Game game, Ability source) {
        if (game != null && game.inCheckPlayableState()) {
            ObjectColor color = (ObjectColor) game.getState().getValue(source.getSourceId() + "_color");
            if (color != null) {
                List<Mana> availableNetMana = new ArrayList<>();
                availableNetMana.add(new Mana(ColoredManaSymbol.lookup(color.toString().charAt(0))));
                return availableNetMana;
            }
        }
        return super.getNetMana(game, source);
    }

    @Override
    public Mana produceMana(Game game, Ability source) {
        if (game != null) {
            ObjectColor color = (ObjectColor) game.getState().getValue(source.getSourceId() + "_color");
            if (color != null) {
                return new Mana(ColoredManaSymbol.lookup(color.toString().charAt(0)));
            }
        }
        return new Mana();
    }

    @Override
    public CagedSunEffect copy() {
        return new CagedSunEffect(this);
    }

}
