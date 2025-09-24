package mage.cards.s;

import mage.MageInt;
import mage.MageItem;
import mage.abilities.Ability;
import mage.abilities.ActivatedAbility;
import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.effects.common.ExileTargetEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.StaticFilters;
import mage.filter.common.FilterLandCard;
import mage.game.ExileZone;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.target.common.TargetCardInYourGraveyard;
import mage.util.CardUtil;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 *
 * @author Jmlundeen
 */
public final class StewardOfTheHarvest extends CardImpl {

    private static final FilterLandCard filter = new FilterLandCard("land cards from your graveyard");

    public StewardOfTheHarvest(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{3}{G}");
        
        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.DRUID);
        this.power = new MageInt(3);
        this.toughness = new MageInt(3);

        // When this creature enters, exile up to three target land cards from your graveyard.
        Ability ability = new EntersBattlefieldTriggeredAbility(new ExileTargetEffect().setToSourceExileZone(true));
        ability.addTarget(new TargetCardInYourGraveyard(0, 3, filter));
        this.addAbility(ability);

        // Creatures you control have all activated abilities of all land cards exiled with this creature.
        this.addAbility(new SimpleStaticAbility(new StewardOfTheHarvestEffect()));
    }

    private StewardOfTheHarvest(final StewardOfTheHarvest card) {
        super(card);
    }

    @Override
    public StewardOfTheHarvest copy() {
        return new StewardOfTheHarvest(this);
    }
}

class StewardOfTheHarvestEffect extends ContinuousEffectImpl {

    public StewardOfTheHarvestEffect() {
        super(Duration.WhileOnBattlefield, Layer.AbilityAddingRemovingEffects_6, SubLayer.NA, Outcome.AddAbility);
        staticText = "Creatures you control have all activated abilities of all land cards exiled with this creature.";
    }

    private StewardOfTheHarvestEffect(StewardOfTheHarvestEffect effect) {
        super(effect);
    }

    @Override
    public StewardOfTheHarvestEffect copy() {
        return new StewardOfTheHarvestEffect(this);
    }

    @Override
    public void applyToObjects(Layer layer, SubLayer sublayer, Ability source, Game game, List<MageItem> affectedObjects) {
        UUID exileId = CardUtil.getExileZoneId(game, source.getSourceId(), CardUtil.getActualSourceObjectZoneChangeCounter(game, source));
        ExileZone exile = game.getExile().getExileZone(exileId);
        List<Ability> abilities = exile.getCards(game).stream()
                .map(card -> card.getAbilities(game))
                .flatMap(List::stream)
                .filter(ability -> ability instanceof ActivatedAbility)
                .collect(Collectors.toList());
        for (MageItem object : affectedObjects) {
            for (Ability ability : abilities) {
                ((Permanent) object).addAbility(ability, source.getSourceId(), game);
            }
        }
    }

    @Override
    public boolean queryAffectedObjects(Layer layer, Ability source, Game game, List<MageItem> affectedObjects) {
        UUID exileId = CardUtil.getExileZoneId(game, source.getSourceId(), CardUtil.getActualSourceObjectZoneChangeCounter(game, source));
        ExileZone exile = game.getExile().getExileZone(exileId);
        if (exile == null) {
            return false;
        }
        affectedObjects.addAll(game.getBattlefield().getActivePermanents(StaticFilters.FILTER_CONTROLLED_CREATURE, source.getControllerId(), source, game));
        return !affectedObjects.isEmpty();
    }
}