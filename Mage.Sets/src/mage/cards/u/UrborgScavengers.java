package mage.cards.u;

import mage.MageInt;
import mage.MageItem;
import mage.abilities.Ability;
import mage.abilities.common.EntersBattlefieldOrAttacksSourceTriggeredAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.effects.common.ExileTargetForSourceEffect;
import mage.abilities.effects.common.counter.AddCountersSourceEffect;
import mage.abilities.keyword.*;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.counters.CounterType;
import mage.game.ExileZone;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.target.common.TargetCardInGraveyard;
import mage.util.CardUtil;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author TheElk801
 */
public final class UrborgScavengers extends CardImpl {

    public UrborgScavengers(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{2}{B}");

        this.subtype.add(SubType.SPIRIT);
        this.power = new MageInt(2);
        this.toughness = new MageInt(2);

        // Whenever Urborg Scavengers enters the battlefield or attacks, exile target card from a graveyard. Put a +1/+1 counter on Urborg Scavengers.
        Ability ability = new EntersBattlefieldOrAttacksSourceTriggeredAbility(new ExileTargetForSourceEffect());
        ability.addTarget(new TargetCardInGraveyard());
        ability.addEffect(new AddCountersSourceEffect(CounterType.P1P1.createInstance()));
        this.addAbility(ability);

        // Urborg Scavengers has flying as long as a card exiled with it has flying. The same is true for first strike, double strike, deathtouch, haste, hexproof, indestructible, lifelink, menace, reach, trample, and vigilance.
        this.addAbility(new SimpleStaticAbility(new UrborgScavengersEffect()));
    }

    private UrborgScavengers(final UrborgScavengers card) {
        super(card);
    }

    @Override
    public UrborgScavengers copy() {
        return new UrborgScavengers(this);
    }
}

class UrborgScavengersEffect extends ContinuousEffectImpl {

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
            VigilanceAbility.class
    ));

    UrborgScavengersEffect() {
        super(Duration.WhileOnBattlefield, Layer.AbilityAddingRemovingEffects_6, SubLayer.NA, Outcome.AddAbility);
        staticText = "{this} has flying as long as a card exiled with it has flying. " +
                "The same is true for first strike, double strike, deathtouch, haste, " +
                "hexproof, indestructible, lifelink, menace, reach, trample, and vigilance";
    }

    private UrborgScavengersEffect(final UrborgScavengersEffect effect) {
        super(effect);
    }

    @Override
    public UrborgScavengersEffect copy() {
        return new UrborgScavengersEffect(this);
    }

    @Override
    public void applyToObjects(Layer layer, SubLayer sublayer, Ability source, Game game, List<MageItem> affectedObjects) {
        Set<Ability> abilities = game.getExile()
                .getExileZone(CardUtil.getExileZoneId(game, source.getSourceId(), game.getState().getZoneChangeCounter(source.getSourceId())))
                .getCards(game)
                .stream()
                .map(card -> card.getAbilities(game))
                .flatMap(Collection::stream)
                .filter(ability -> KEYWORD_ABILITIES.stream()
                        .anyMatch(aClass -> aClass.isAssignableFrom(ability.getClass())))
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
        ExileZone exileZone = game.getExile()
                .getExileZone(CardUtil.getExileZoneId(game, source.getSourceId(), sourcePermanent.getZoneChangeCounter(game)));
        if (exileZone == null) {
            return false;
        }
        affectedObjects.add(sourcePermanent);
        return true;
    }
}
