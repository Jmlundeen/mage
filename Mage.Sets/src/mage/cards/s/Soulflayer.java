package mage.cards.s;

import mage.MageInt;
import mage.MageItem;
import mage.MageObjectReference;
import mage.abilities.Ability;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.keyword.*;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.cards.Cards;
import mage.constants.*;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.util.CardUtil;

import java.util.*;

/**
 * @author LevelX2
 */
public final class Soulflayer extends CardImpl {

    public Soulflayer(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{4}{B}{B}");
        this.subtype.add(SubType.DEMON);
        this.power = new MageInt(4);
        this.toughness = new MageInt(4);

        // Delve
        this.addAbility(new DelveAbility(true));

        // If a creature card with flying was exiled with Soulflayer's delve ability, Soulflayer has flying. The same is true for first strike, double strike, deathtouch, haste, hexproof, indestructible, lifelink, reach, trample, and vigilance.
        this.addAbility(new SimpleStaticAbility(new SoulflayerEffect()));

    }

    private Soulflayer(final Soulflayer card) {
        super(card);
    }

    @Override
    public Soulflayer copy() {
        return new Soulflayer(this);
    }
}

class SoulflayerEffect extends ContinuousEffectImpl {

    private static final Set<Class<? extends Ability>> KEYWORD_ABILITIES = new HashSet<>(Arrays.asList(
            FlyingAbility.class,
            FirstStrikeAbility.class,
            DoubleStrikeAbility.class,
            HasteAbility.class,
            HexproofBaseAbility.class,
            IndestructibleAbility.class,
            LifelinkAbility.class,
            ReachAbility.class,
            TrampleAbility.class,
            VigilanceAbility.class
    ));
    private MageObjectReference objectReference = null;

    public SoulflayerEffect() {
        super(Duration.WhileOnBattlefield, Layer.AbilityAddingRemovingEffects_6, SubLayer.NA, Outcome.AddAbility);
        staticText = "If a creature card with flying was exiled with {this}'s delve ability, {this} has flying. " +
                "The same is true for first strike, double strike, deathtouch, haste, hexproof, indestructible, lifelink, reach, trample, and vigilance";
    }

    private SoulflayerEffect(final SoulflayerEffect effect) {
        super(effect);
        this.objectReference = effect.objectReference;
    }

    @Override
    public SoulflayerEffect copy() {
        return new SoulflayerEffect(this);
    }

    @Override
    public void applyToObjects(Layer layer, SubLayer sublayer, Ability source, Game game, List<MageItem> affectedObjects) {
        Set<Ability> exileAbilities = new HashSet<>();
        getAbilitiesInExile(game, source, exileAbilities);

        for (MageItem object : affectedObjects) {
            Permanent permanent = (Permanent) object;
            for (Ability ability : exileAbilities) {
                if (isValidKeywordAbility(ability.getClass())) {
                    permanent.addAbility(ability, source.getSourceId(), game);
                }
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

    private void getAbilitiesInExile(Game game, Ability source, Set<Ability> exileAbilities) {
        String keyString = CardUtil.getCardZoneString("delvedCards", source.getSourceId(), game, true);
        Cards delvedCards = (Cards) game.getState().getValue(keyString);
        for (Card card : delvedCards.getCards(game)) {
            for (Ability ability : card.getAbilities(game)) {
                if (isValidKeywordAbility(ability.getClass())) {
                    exileAbilities.add(ability);
                }
            }
        }
    }

    private boolean isValidKeywordAbility(Class<? extends Ability> abilityClass) {
        return KEYWORD_ABILITIES.stream()
                .anyMatch(keywordClass ->
                        keywordClass.isAssignableFrom(abilityClass)
                );
    }
}
