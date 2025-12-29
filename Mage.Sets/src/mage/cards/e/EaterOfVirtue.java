package mage.cards.e;

import mage.MageItem;
import mage.abilities.Ability;
import mage.abilities.common.DiesAttachedTriggeredAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.costs.mana.GenericManaCost;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.continuous.BoostEquippedEffect;
import mage.abilities.keyword.*;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.StaticFilters;
import mage.game.ExileZone;
import mage.game.Game;
import mage.game.MoveCardsParameters;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.util.CardUtil;

import java.util.*;

/**
 * @author jeffwadsworth
 */
public final class EaterOfVirtue extends CardImpl {

    public EaterOfVirtue(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT}, "{1}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.EQUIPMENT);

        // Whenever equipped creature dies, exile it.
        this.addAbility(new DiesAttachedTriggeredAbility(new EaterOfVirtueExileEffect(), "equipped creature", false, true, SetTargetPointer.CARD));

        // Equipped creature gets +2/+0.
        this.addAbility(new SimpleStaticAbility(new BoostEquippedEffect(2, 0, Duration.WhileOnBattlefield)));

        // As long as a card exiled with Eater of Virtue has flying, equipped creature has flying. The same is true for first strike, double strike, deathtouch, haste, hexproof, indestructible, lifelink, menace, protection, reach, trample, and vigilance.
        this.addAbility(new SimpleStaticAbility(new EaterOfVirtueGainAbilityAttachedEffect()));

        // Equip {1}
        this.addAbility(new EquipAbility(Outcome.BoostCreature, new GenericManaCost(1), false));

    }

    private EaterOfVirtue(final EaterOfVirtue card) {
        super(card);
    }

    @Override
    public EaterOfVirtue copy() {
        return new EaterOfVirtue(this);
    }
}

class EaterOfVirtueExileEffect extends OneShotEffect {

    EaterOfVirtueExileEffect() {
        super(Outcome.Neutral);
        this.staticText = "exile it";
    }

    private EaterOfVirtueExileEffect(final EaterOfVirtueExileEffect effect) {
        super(effect);
    }

    @Override
    public EaterOfVirtueExileEffect copy() {
        return new EaterOfVirtueExileEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        Permanent eaterOfVirtue = game.getPermanent(source.getSourceId());
        Card exiledCard = game.getCard(getTargetPointer().getFirst(game, source));
        if (controller != null
                && eaterOfVirtue != null
                && exiledCard != null) {
            MoveCardsParameters parameters = new MoveCardsParameters(exiledCard, Zone.EXILED)
                    .setExileId(source.getSourceId())
                    .setExileName(CardUtil.createObjectRelatedWindowTitle(source, game, null));
            controller.moveCards(parameters, source, game);
            return true;
        }
        return false;
    }
}

class EaterOfVirtueGainAbilityAttachedEffect extends ContinuousEffectImpl {

    private static final Set<Class<? extends Ability>> KEYWORD_ABILITIES = new HashSet<>(Arrays.asList(
            FlyingAbility.class,
            FirstStrikeAbility.class,
            DoubleStrikeAbility.class,
            DeathtouchAbility.class,
            HasteAbility.class,
            HexproofAbility.class,
            IndestructibleAbility.class,
            LifelinkAbility.class,
            MenaceAbility.class,
            ProtectionAbility.class,
            ReachAbility.class,
            TrampleAbility.class,
            VigilanceAbility.class
    ));

    EaterOfVirtueGainAbilityAttachedEffect() {
        super(Duration.WhileOnBattlefield, Layer.AbilityAddingRemovingEffects_6, SubLayer.NA, Outcome.AddAbility);
        staticText = "As long as a card exiled with {this} has flying, equipped creature has flying. The same is true for first strike, double strike, deathtouch, haste, hexproof, indestructible, lifelink, menace, protection, reach, trample, and vigilance";
    }

    private EaterOfVirtueGainAbilityAttachedEffect(final EaterOfVirtueGainAbilityAttachedEffect effect) {
        super(effect);
    }

    @Override
    public EaterOfVirtueGainAbilityAttachedEffect copy() {
        return new EaterOfVirtueGainAbilityAttachedEffect(this);
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
        Permanent permanent = game.getPermanent(sourcePermanent.getAttachedTo());
        if (permanent == null) {
            return false;
        }
        affectedObjects.add(permanent);
        return true;
    }

    private void getAbilitiesInExile(Game game, Ability source, Set<Ability> exileAbilities) {
        ExileZone exileZone = game.getState().getExile().getExileZone(source.getSourceId());
        if (exileZone == null || exileZone.isEmpty()) {
            return;
        }
        for (Card card : exileZone.getCards(StaticFilters.FILTER_CARD_CREATURE, game)) {
            exileAbilities.addAll(card.getAbilities(game));
        }
    }

    private boolean isValidKeywordAbility(Class<? extends Ability> abilityClass) {
        return KEYWORD_ABILITIES.stream()
                .anyMatch(keywordClass ->
                        keywordClass.isAssignableFrom(abilityClass)
                );
    }
}
