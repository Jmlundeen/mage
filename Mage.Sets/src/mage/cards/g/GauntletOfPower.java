package mage.cards.g;

import mage.Mana;
import mage.ObjectColor;
import mage.abilities.Ability;
import mage.abilities.common.AsEntersBattlefieldAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.Effect;
import mage.abilities.effects.common.ChooseColorEffect;
import mage.abilities.effects.common.continuous.BoostAllEffect;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.abilities.mana.TriggeredManaAbility;
import mage.abilities.mana.providers.ManaPlayerProvider;
import mage.abilities.mana.providers.common.manaType.ChosenColorTypeProvider;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.common.FilterCreaturePermanent;
import mage.filter.predicate.mageobject.ChosenColorPredicate;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.game.events.TappedForManaEvent;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.target.targetpointer.FixedTarget;

import java.util.Set;
import java.util.UUID;

/**
 * @author LevelX2
 */
public final class GauntletOfPower extends CardImpl {

    private static final FilterCreaturePermanent filter
            = new FilterCreaturePermanent("creatures of the chosen color");

    static {
        filter.add(ChosenColorPredicate.TRUE);
    }

    public GauntletOfPower(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT}, "{5}");

        // As Gauntlet of Power enters the battlefield, choose a color.
        this.addAbility(new AsEntersBattlefieldAbility(new ChooseColorEffect(Outcome.Neutral)));

        // Creatures of the chosen color get +1/+1.
        this.addAbility(new SimpleStaticAbility(new BoostAllEffect(
                1, 1, Duration.WhileOnBattlefield, filter, false
        )));

        // Whenever a basic land is tapped for mana of the chosen color, its controller adds one mana of that color.
        this.addAbility(new GauntletOfPowerTapForManaAllTriggeredAbility());
    }

    private GauntletOfPower(final GauntletOfPower card) {
        super(card);
    }

    @Override
    public GauntletOfPower copy() {
        return new GauntletOfPower(this);
    }
}

enum GauntletOfPowerPlayerProvider implements ManaPlayerProvider {
    instance;

    @Override
    public Player getManaPlayer(Game game, Ability source, Effect effect) {
        return game.getPlayer(effect.getTargetPointer().getFirst(game, source));
    }
}

class GauntletOfPowerTapForManaAllTriggeredAbility extends TriggeredManaAbility {

    GauntletOfPowerTapForManaAllTriggeredAbility() {
        super(Zone.BATTLEFIELD, new ComposedManaAbilityBuilder()
                .addEach(ChosenColorTypeProvider.instance, 1)
                .playerProvider(GauntletOfPowerPlayerProvider.instance)
                .buildEffect(), false);
    }

    private GauntletOfPowerTapForManaAllTriggeredAbility(GauntletOfPowerTapForManaAllTriggeredAbility ability) {
        super(ability);
    }

    @Override
    public boolean checkEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.TAPPED_FOR_MANA;
    }

    @Override
    public boolean checkTrigger(GameEvent event, Game game) {
        TappedForManaEvent mEvent = (TappedForManaEvent) event;
        Permanent permanent = mEvent.getPermanent();
        if (permanent == null || !permanent.isLand(game) || !permanent.isBasic(game)) {
            return false;
        }
        ObjectColor color = (ObjectColor) game.getState().getValue(getSourceId() + "_color");
        if (color == null) {
            return false;
        }
        Mana mana = mEvent.getMana();
        Set<ManaType> manaTypes = ManaType.getManaTypesFromManaList(mana);
        Set<ManaType> chosenColorManaTypes = ManaType.getManaTypesFromObjectColor(color);
        getEffects().setTargetPointer(new FixedTarget(permanent.getControllerId(), game));
        return chosenColorManaTypes.stream().anyMatch(manaTypes::contains);
    }

    @Override
    public GauntletOfPowerTapForManaAllTriggeredAbility copy() {
        return new GauntletOfPowerTapForManaAllTriggeredAbility(this);
    }

    @Override
    public String getRule() {
        return "Whenever a basic land is tapped for mana of the chosen color, "
                + "its controller adds an additional one mana of that color.";
    }
}
