package mage.cards.a;

import mage.MageItem;
import mage.abilities.Ability;
import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.costs.common.SacrificeTargetCost;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.dynamicvalue.DynamicValue;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.effects.Effect;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.ReturnSourceFromGraveyardToHandEffect;
import mage.abilities.effects.common.continuous.SetBasePowerToughnessSourceEffect;
import mage.abilities.keyword.*;
import mage.cards.Card;
import mage.cards.CardSetInfo;
import mage.cards.TransformingDoubleFacedCard;
import mage.constants.*;
import mage.filter.StaticFilters;
import mage.game.ExileZone;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.util.CardUtil;

import java.util.*;
import java.util.stream.Collectors;

/**
 *
 * @author jeffwadsworth
 */
public final class AltarOfTheWretched extends TransformingDoubleFacedCard {

    public AltarOfTheWretched(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, 
                new CardType[]{CardType.ARTIFACT}, new SubType[]{}, "{2}{B}",
                "Wretched Bonemass",
                new CardType[]{CardType.CREATURE}, new SubType[]{SubType.SKELETON, SubType.HORROR}, "B");
        this.getRightHalfCard().setPT(0, 0);

        // When Altar of the Wretched enters the battlefield, you may sacrifice a nontoken creature. If you do, draw X cards, then mill X cards, where X is that creature’s power.
        this.getLeftHalfCard().addAbility(new EntersBattlefieldTriggeredAbility(new AltarOfTheWretchedEffect(), true));

        // Craft with one or more creatures {2}{B}{B}
        this.getLeftHalfCard().addAbility(new CraftAbility(
                "{2}{B}{B}", "one or more creatures", "other creatures you control and/or"
                + "creature cards in your graveyard", 1, Integer.MAX_VALUE, CardType.CREATURE.getPredicate()
        ));

        // {2}{B}: Return Altar of the Wretched from your graveyard to your hand.
        this.getLeftHalfCard().addAbility(new SimpleActivatedAbility(Zone.GRAVEYARD, new ReturnSourceFromGraveyardToHandEffect(), new ManaCostsImpl<>("{2}{B}")));

        // Wretched Bonemass
        // Wretched Bonemass’s power and toughness are each equal to the total power of the exiled cards used to craft it.
        this.getRightHalfCard().addAbility(new SimpleStaticAbility(new SetBasePowerToughnessSourceEffect(WretchedBonemassDynamicValue.instance).setText("{this}'s power and toughness are each equal to the total power of the exiled cards used to craft it.")));

        // Wretched Bonemass has flying as long as an exiled card used to craft it has flying. The same is true for first strike, double strike, deathtouch, haste, hexproof, indestructible, lifelink, menace, protection, reach, trample, and vigilance.
        this.getRightHalfCard().addAbility(new SimpleStaticAbility(new WretchedBonemassGainAbilityEffect()));
    }

    private AltarOfTheWretched(final AltarOfTheWretched card) {
        super(card);
    }

    @Override
    public AltarOfTheWretched copy() {
        return new AltarOfTheWretched(this);
    }
}

class AltarOfTheWretchedEffect extends OneShotEffect {

    AltarOfTheWretchedEffect() {
        super(Outcome.Benefit);
        staticText = "you may sacrifice a nontoken creature. If you do, draw X cards, then mill X cards, where X is that creature's power.";
    }

    private AltarOfTheWretchedEffect(final AltarOfTheWretchedEffect effect) {
        super(effect);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller != null) {
            SacrificeTargetCost cost = new SacrificeTargetCost(1, StaticFilters.FILTER_CONTROLLED_CREATURE_NON_TOKEN);
            if (cost.canPay(source, source, controller.getId(), game)
                    && cost.pay(source, game, source, controller.getId(), true)) {
                final int power = cost.getPermanents().get(0).getPower().getValue();
                controller.drawCards(power, source, game);
                controller.millCards(power, source, game);
                return true;
            }
        }
        return false;
    }

    @Override
    public AltarOfTheWretchedEffect copy() {
        return new AltarOfTheWretchedEffect(this);
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
                        game, permanent.getMainCard().getId(), permanent.getZoneChangeCounter(game) - 1
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
        Permanent sourcePermanent = game.getPermanent(source.getSourceId());

        Set<Ability> abilities = game.getExile()
                .getExileZone(CardUtil.getExileZoneId(game,
                        sourcePermanent.getMainCard().getId(),
                        sourcePermanent.getZoneChangeCounter(game) - 1))
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
                .getExileZone(CardUtil.getExileZoneId(game,
                        sourcePermanent.getMainCard().getId(),
                        sourcePermanent.getZoneChangeCounter(game) - 1)
                );
        if (exileZone == null) {
            return false;
        }
        affectedObjects.add(sourcePermanent);
        return true;
    }
}