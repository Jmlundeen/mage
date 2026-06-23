package mage.cards.c;

import mage.ObjectColor;
import mage.abilities.common.AsEntersBattlefieldAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.common.ChooseColorEffect;
import mage.abilities.effects.common.continuous.BoostAllOfChosenColorEffect;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.abilities.mana.TriggeredManaAbility;
import mage.abilities.mana.providers.common.manaType.ChosenColorTypeProvider;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.Outcome;
import mage.constants.Zone;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.game.permanent.Permanent;

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
        super(Zone.BATTLEFIELD, new ComposedManaAbilityBuilder()
                .addEach(ChosenColorTypeProvider.instance, 1)
                .buildEffect());
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

