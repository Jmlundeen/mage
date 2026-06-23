package mage.cards.p;

import mage.MageObject;
import mage.ObjectColor;
import mage.abilities.Ability;
import mage.abilities.common.EntersBattlefieldTappedAbility;
import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.dynamicvalue.common.StaticValue;
import mage.abilities.effects.Effect;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.hint.Hint;
import mage.abilities.mana.ColorlessManaAbility;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.abilities.mana.providers.ManaTypeProvider;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.cards.Cards;
import mage.cards.CardsImpl;
import mage.constants.*;
import mage.game.ExileZone;
import mage.game.Game;
import mage.game.MoveCardsParameters;
import mage.players.Player;
import mage.target.common.TargetCardInGraveyard;
import mage.util.CardUtil;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Susucr
 */
public final class PitOfOfferings extends CardImpl {

    public PitOfOfferings(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.LAND}, "");

        this.subtype.add(SubType.CAVE);

        // Pit of Offerings enters the battlefield tapped.
        this.addAbility(new EntersBattlefieldTappedAbility());

        // When Pit of Offerings enters the battlefield, exile up to three target cards from graveyards.
        Ability ability = new EntersBattlefieldTriggeredAbility(new PitOfOfferingsEffect(), false);
        ability.addTarget(new TargetCardInGraveyard(0, 3));
        this.addAbility(ability);

        // {T}: Add {C}.
        this.addAbility(new ColorlessManaAbility());

        // {T}: Add one mana of any of the exiled cards' colors.
        this.addAbility(new ComposedManaAbilityBuilder()
                .addDynamicChoice(StaticValue.get(1), PitOfOfferingsManaTypes.instance)
                .cost(new TapSourceCost())
                .ruleText("Add one mana of any of the exiled cards' colors")
                .build()
                .addHint(PitOfOfferingsHint.instance)
        );
    }

    private PitOfOfferings(final PitOfOfferings card) {
        super(card);
    }

    @Override
    public PitOfOfferings copy() {
        return new PitOfOfferings(this);
    }
}

enum PitOfOfferingsHint implements Hint {
    instance;

    @Override
    public String getText(Game game, Ability ability) {
        MageObject sourceObject = ability.getSourceObject(game);
        if (sourceObject == null) {
            return "";
        }

        Set<ObjectColor> exiledCardsColors = PitOfOfferingsManaTypes.getColorsExiled(sourceObject, game);

        List<String> manaText = new ArrayList<>();
        if (exiledCardsColors.stream().anyMatch(ObjectColor::isWhite)) {
            manaText.add("{W}");
        }
        if (exiledCardsColors.stream().anyMatch(ObjectColor::isBlue)) {
            manaText.add("{U}");
        }
        if (exiledCardsColors.stream().anyMatch(ObjectColor::isBlack)) {
            manaText.add("{B}");
        }
        if (exiledCardsColors.stream().anyMatch(ObjectColor::isRed)) {
            manaText.add("{R}");
        }
        if (exiledCardsColors.stream().anyMatch(ObjectColor::isGreen)) {
            manaText.add("{G}");
        }

        if (manaText.isEmpty()) {
            return "";
        }

        return "Color of cards exiled: " + manaText.stream().collect(Collectors.joining(", "));
    }

    @Override
    public PitOfOfferingsHint copy() {
        return this;
    }
}

/**
 * Inspired by {@link mage.cards.c.ChromeMox}
 */
class PitOfOfferingsEffect extends OneShotEffect {

    static UUID getExileZoneId(MageObject sourceObject, Game game) {
        if (sourceObject == null) {
            return null;
        }
        return CardUtil.getExileZoneId(CardUtil.getObjectZoneString(
                "_pitOfOfferingExile", sourceObject.getId(), game,
                sourceObject.getZoneChangeCounter(game), false
        ), game);
    }

    public PitOfOfferingsEffect() {
        super(Outcome.Benefit);
        staticText = "exile up to three target cards from graveyards";
    }

    private PitOfOfferingsEffect(final PitOfOfferingsEffect effect) {
        super(effect);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        MageObject sourceObject = source.getSourceObject(game);
        if (controller == null || sourceObject == null) {
            return false;
        }
        Cards cardsExiled = new CardsImpl(getTargetPointer().getTargets(game, source));
        MoveCardsParameters parameters = new MoveCardsParameters(cardsExiled.getCards(game), Zone.EXILED)
                .setExileId(CardUtil.getExileZoneId(game, source))
                .setExileName(CardUtil.createObjectRelatedWindowTitle(source, game, null));
        controller.moveCards(parameters, source, game);
        return true;
    }

    @Override
    public PitOfOfferingsEffect copy() {
        return new PitOfOfferingsEffect(this);
    }

}

enum PitOfOfferingsManaTypes implements ManaTypeProvider {
    instance;

    static Set<ObjectColor> getColorsExiled(MageObject sourceObject, Game game) {
        if (game == null) {
            return new HashSet<>();
        }
        ExileZone exileZone = game
                .getExile()
                .getExileZone(PitOfOfferingsEffect.getExileZoneId(sourceObject, game));

        if (exileZone == null) {
            return new HashSet<>();
        }
        return exileZone
                .getCards(game)
                .stream()
                .map(c -> c.getColor(game))
                .collect(Collectors.toSet());
    }

    @Override
    public Set<ManaType> getManaTypes(Game game, Ability source, Effect effect) {
        if (game == null || source == null) {
            return EnumSet.noneOf(ManaType.class);
        }
        MageObject sourceObject = source.getSourceObject(game);
        if (sourceObject == null) {
            return EnumSet.noneOf(ManaType.class);
        }
        Set<ManaType> manaTypes = EnumSet.noneOf(ManaType.class);
        for (ObjectColor color : getColorsExiled(sourceObject, game)) {
            manaTypes.addAll(ManaType.getManaTypesFromObjectColor(color));
        }
        return manaTypes;
    }
}
