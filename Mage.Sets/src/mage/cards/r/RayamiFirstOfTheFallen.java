package mage.cards.r;

import mage.MageInt;
import mage.MageItem;
import mage.abilities.Ability;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.effects.ReplacementEffectImpl;
import mage.abilities.keyword.*;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.counters.CounterType;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.game.events.ZoneChangeEvent;
import mage.game.permanent.Permanent;
import mage.game.permanent.PermanentToken;
import mage.players.Player;
import mage.util.CardUtil;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author TheElk801
 */
public final class RayamiFirstOfTheFallen extends CardImpl {

    public RayamiFirstOfTheFallen(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{1}{B}{G}{U}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.VAMPIRE);
        this.power = new MageInt(5);
        this.toughness = new MageInt(4);

        // If a nontoken creature would die, exile that card with a blood counter on it instead.
        this.addAbility(new SimpleStaticAbility(new RayamiFirstOfTheFallenReplacementEffect()));

        // As long as an exiled creature card with a blood counter on it has flying, Rayami, First of the Fallen has flying. The same is true for first strike, double strike, deathtouch, haste, hexproof, indestructible, lifelink, menace, protection, reach, trample, and vigilance.
        this.addAbility(new SimpleStaticAbility(new RayamiFirstOfTheFallenEffect()));
    }

    private RayamiFirstOfTheFallen(final RayamiFirstOfTheFallen card) {
        super(card);
    }

    @Override
    public RayamiFirstOfTheFallen copy() {
        return new RayamiFirstOfTheFallen(this);
    }

}

class RayamiFirstOfTheFallenEffect extends ContinuousEffectImpl {

    private static final Set<Class<? extends Ability>> KEYWORD_ABILITIES = new HashSet<>(Arrays.asList(
            FlyingAbility.class,
            FirstStrikeAbility.class,
            DoubleStrikeAbility.class,
            DeathtouchAbility.class,
            HasteAbility.class,
            HexproofBaseAbility.class,
            IndestructibleAbility.class,
            LifelinkAbility.class,
            MenaceAbility.class,
            ReachAbility.class,
            TrampleAbility.class,
            VigilanceAbility.class,
            ProtectionAbility.class
    ));

    RayamiFirstOfTheFallenEffect() {
        super(Duration.WhileOnBattlefield, Layer.AbilityAddingRemovingEffects_6, SubLayer.NA, Outcome.AddAbility);
        this.addDependedToType(DependencyType.AddingAbility);
        staticText = "As long as an exiled creature card with a blood counter on it has flying, " +
                "{this} has flying. The same is true for first strike, double strike, deathtouch, haste, " +
                "hexproof, indestructible, lifelink, menace, protection, reach, trample, and vigilance.";
    }

    private RayamiFirstOfTheFallenEffect(final RayamiFirstOfTheFallenEffect effect) {
        super(effect);
    }

    @Override
    public void applyToObjects(Layer layer, SubLayer sublayer, Ability source, Game game, List<MageItem> affectedObjects) {
        Set<Ability> abilities = game.getExile()
                .getCardsInRange(game, source.getControllerId())
                .stream()
                .filter(card1 -> card1.isCreature(game))
                .filter(card -> card.getCounters(game).getCount(CounterType.BLOOD) > 0)
                .map(card -> card.getAbilities(game))
                .flatMap(Collection::stream)
                .filter(ability -> KEYWORD_ABILITIES.stream()
                        .anyMatch(aClass -> ability.getClass().isAssignableFrom(aClass)))
                .collect(Collectors.toSet());
        for (MageItem object : affectedObjects) {
            for (Ability ability : abilities) {
                ((Permanent) object).addAbility(ability, source.getSourceId(), game);
            }
        }
    }

    @Override
    public boolean queryAffectedObjects(Layer layer, Ability source, Game game, List<MageItem> affectedObjects) {
        Permanent sourcePermanent = game.getPermanent(source.getSourceId());
        if (sourcePermanent == null) {
            return false;
        }
        affectedObjects.add(sourcePermanent);
        return true;
    }

    @Override
    public RayamiFirstOfTheFallenEffect copy() {
        return new RayamiFirstOfTheFallenEffect(this);
    }
}

class RayamiFirstOfTheFallenReplacementEffect extends ReplacementEffectImpl {

    RayamiFirstOfTheFallenReplacementEffect() {
        super(Duration.WhileOnBattlefield, Outcome.Exile);
        staticText = "If a nontoken creature would die, exile that card with a blood counter on it instead";
    }

    private RayamiFirstOfTheFallenReplacementEffect(final RayamiFirstOfTheFallenReplacementEffect effect) {
        super(effect);
    }

    @Override
    public RayamiFirstOfTheFallenReplacementEffect copy() {
        return new RayamiFirstOfTheFallenReplacementEffect(this);
    }

    @Override
    public boolean replaceEvent(GameEvent event, Ability source, Game game) {
        Permanent permanent = ((ZoneChangeEvent) event).getTarget();
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null
                || permanent == null
                || (permanent instanceof PermanentToken)) {
            return false;
        }
        return CardUtil.moveCardWithCounter(game, source, controller, permanent, Zone.EXILED, CounterType.BLOOD.createInstance());
    }

    @Override
    public boolean checksEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.ZONE_CHANGE;
    }

    @Override
    public boolean applies(GameEvent event, Ability source, Game game) {
        ZoneChangeEvent zce = (ZoneChangeEvent) event;
        return zce.isDiesEvent()
                && zce.getTarget().isCreature(game)
                && !(zce.getTarget() instanceof PermanentToken);
    }
}
