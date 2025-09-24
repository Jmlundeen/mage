package mage.cards.w;

import mage.MageInt;
import mage.MageItem;
import mage.abilities.Ability;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.dynamicvalue.DynamicValue;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.effects.Effect;
import mage.abilities.effects.common.continuous.SetBasePowerToughnessSourceEffect;
import mage.abilities.keyword.*;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.game.ExileZone;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.util.CardUtil;

import java.util.*;
import java.util.stream.Collectors;

/**
 *
 * @author jeffwadsworth
 */
public final class WretchedBonemass extends CardImpl {

    public WretchedBonemass(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, null);
        this.nightCard = true;
        this.color.setBlack(true);

        this.subtype.add(SubType.SKELETON);
        this.subtype.add(SubType.HORROR);
        this.power = new MageInt(0);
        this.toughness = new MageInt(0);

        // Wretched Bonemass’s power and toughness are each equal to the total power of the exiled cards used to craft it.
        this.addAbility(new SimpleStaticAbility(new SetBasePowerToughnessSourceEffect(WretchedBonemassDynamicValue.instance).setText("{this}'s power and toughness are each equal to the total power of the exiled cards used to craft it.")));

        // Wretched Bonemass has flying as long as an exiled card used to craft it has flying. The same is true for first strike, double strike, deathtouch, haste, hexproof, indestructible, lifelink, menace, protection, reach, trample, and vigilance.
        this.addAbility(new SimpleStaticAbility(new WretchedBonemassGainAbilityEffect()));

    }

    private WretchedBonemass(final WretchedBonemass card) {
        super(card);
    }

    @Override
    public WretchedBonemass copy() {
        return new WretchedBonemass(this);
    }
}

enum WretchedBonemassDynamicValue implements DynamicValue {
    instance;

    @Override
    public int calculate(Game game, Ability sourceAbility, Effect effect) {
        int totalPower = 0;
        Permanent permanent = sourceAbility.getSourcePermanentIfItStillExists(game);
        if (permanent == null) {
            return 0;
        }
        ExileZone exileZone = game
                .getExile()
                .getExileZone(CardUtil.getExileZoneId(
                        game, permanent.getId(), permanent.getZoneChangeCounter(game) - 2
                ));
        if (exileZone == null) {
            return 0;
        }
        for (Card card : exileZone.getCards(game)) {
            totalPower += card.getPower().getValue();
        }
        return totalPower;
    }

    @Override
    public WretchedBonemassDynamicValue copy() {
        return this;
    }

    @Override
    public String getMessage() {
        return "total power of the exiled cards used to craft it";
    }

    @Override
    public String toString() {
        return "0";
    }
}

class WretchedBonemassGainAbilityEffect extends ContinuousEffectImpl {

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
            ProtectionAbility.class,
            ReachAbility.class,
            TrampleAbility.class,
            VigilanceAbility.class
    ));

    WretchedBonemassGainAbilityEffect() {
        super(Duration.WhileOnBattlefield, Layer.AbilityAddingRemovingEffects_6, SubLayer.NA, Outcome.AddAbility);
        staticText = "{this} has flying as long as an exiled card used to craft it has flying. " +
                "The same is true for first strike, double strike, deathtouch, haste, hexproof, indestructible, " +
                "lifelink, menace, protection, reach, trample, and vigilance.";
    }

    private WretchedBonemassGainAbilityEffect(final WretchedBonemassGainAbilityEffect effect) {
        super(effect);
    }

    @Override
    public WretchedBonemassGainAbilityEffect copy() {
        return new WretchedBonemassGainAbilityEffect(this);
    }

    @Override
    public void applyToObjects(Layer layer, SubLayer sublayer, Ability source, Game game, List<MageItem> affectedObjects) {
        Set<Ability> abilities = game.getExile()
                .getExileZone(CardUtil.getExileZoneId(game, source.getSourceId(), game.getState().getZoneChangeCounter(source.getSourceId()) - 2))
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
