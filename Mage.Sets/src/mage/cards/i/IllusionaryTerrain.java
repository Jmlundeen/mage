package mage.cards.i;

import mage.MageItem;
import mage.MageObject;
import mage.abilities.Ability;
import mage.abilities.common.AsEntersBattlefieldAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.keyword.CumulativeUpkeepAbility;
import mage.abilities.mana.*;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.choices.ChoiceBasicLandType;
import mage.choices.ChoiceImpl;
import mage.constants.*;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.util.CardUtil;

import java.util.List;
import java.util.UUID;

/**
 *
 * @author jeffwadsworth
 */
public final class IllusionaryTerrain extends CardImpl {

    public IllusionaryTerrain(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ENCHANTMENT}, "{U}{U}");

        // Cumulative upkeep {2}
        this.addAbility(new CumulativeUpkeepAbility(new ManaCostsImpl<>("{2}")));

        // As Illusionary Terrain enters the battlefield, choose two basic land types.
        this.addAbility(new AsEntersBattlefieldAbility(new ChooseTwoBasicLandTypesEffect(Outcome.Neutral)));

        // Basic lands of the first chosen type are the second chosen type.
        this.addAbility(new SimpleStaticAbility(new IllusionaryTerrainEffect()));

    }

    private IllusionaryTerrain(final IllusionaryTerrain card) {
        super(card);
    }

    @Override
    public IllusionaryTerrain copy() {
        return new IllusionaryTerrain(this);
    }
}

class IllusionaryTerrainEffect extends ContinuousEffectImpl {

    IllusionaryTerrainEffect() {
        super(Duration.WhileOnBattlefield, Layer.TypeChangingEffects_4, SubLayer.NA, Outcome.Neutral);
        staticText = "Basic lands of the first chosen type are the second chosen type";
    }

    private IllusionaryTerrainEffect(final IllusionaryTerrainEffect effect) {
        super(effect);
    }

    @Override
    public IllusionaryTerrainEffect copy() {
        return new IllusionaryTerrainEffect(this);
    }

    @Override
    public void applyToObjects(Layer layer, SubLayer sublayer, Ability source, Game game, List<MageItem> affectedObjects) {
        for (MageItem object : affectedObjects) {
            Permanent permanent = (Permanent) object;
            // the land mana ability is intrinsic, so add it here, not layer 6
            permanent.removeAllSubTypes(game, SubTypeSet.NonBasicLandType);
            if (permanent.hasSubtype(SubType.FOREST, game)) {
                this.dependencyTypes.add(DependencyType.BecomeForest);
                permanent.addAbility(new GreenManaAbility(), source.getSourceId(), game);
            }
            if (permanent.hasSubtype(SubType.PLAINS, game)) {
                this.dependencyTypes.add(DependencyType.BecomePlains);
                permanent.addAbility(new WhiteManaAbility(), source.getSourceId(), game);
            }
            if (permanent.hasSubtype(SubType.MOUNTAIN, game)) {
                this.dependencyTypes.add(DependencyType.BecomeMountain);
                permanent.addAbility(new RedManaAbility(), source.getSourceId(), game);
            }
            if (permanent.hasSubtype(SubType.ISLAND, game)) {
                this.dependencyTypes.add(DependencyType.BecomeIsland);
                permanent.addAbility(new BlueManaAbility(), source.getSourceId(), game);
            }
            if (permanent.hasSubtype(SubType.SWAMP, game)) {
                this.dependencyTypes.add(DependencyType.BecomeSwamp);
                permanent.addAbility(new BlackManaAbility(), source.getSourceId(), game);
            }
        }
    }

    @Override
    public boolean queryAffectedObjects(Layer layer, Ability source, Game game, List<MageItem> affectedObjects) {
        Player controller = game.getPlayer(source.getControllerId());
        SubType firstChoice = SubType.byDescription((String) game.getState().getValue(source.getSourceId().toString() + "firstChoice"));
        SubType secondChoice = SubType.byDescription((String) game.getState().getValue(source.getSourceId().toString() + "secondChoice"));
        List<Permanent> lands = game.getBattlefield().getActivePermanents(StaticFilters.FILTER_LAND, source.getControllerId(), source, game);
        if (controller == null
                || firstChoice == null
                || secondChoice == null) {
            return false;
        }
        for (Permanent land : lands) {
            if (land.isBasic(game) && land.hasSubtype(firstChoice, game)) {
                affectedObjects.add(land);
            }
        }
        return !affectedObjects.isEmpty();
    }
}

class ChooseTwoBasicLandTypesEffect extends OneShotEffect {

    String choiceOne;
    String choiceTwo;

    public ChooseTwoBasicLandTypesEffect(Outcome outcome) {
        super(outcome);
        this.staticText = "choose two basic land types";
    }

    private ChooseTwoBasicLandTypesEffect(final ChooseTwoBasicLandTypesEffect effect) {
        super(effect);
    }

    @Override
    public ChooseTwoBasicLandTypesEffect copy() {
        return new ChooseTwoBasicLandTypesEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        MageObject mageObject = game.getPermanentEntering(source.getSourceId());
        if (mageObject == null) {
            mageObject = game.getObject(source);
        }
        if (controller != null
                && mageObject != null) {
            ChoiceImpl choices = new ChoiceBasicLandType();
            if (controller.choose(Outcome.Neutral, choices, game)) {
                game.informPlayers(mageObject.getName()
                        + ":  First chosen basic land type is " + choices.getChoice());
                game.getState().setValue(mageObject.getId().toString()
                        + "firstChoice", choices.getChoice());
                choiceOne = SubType.byDescription((String) game.getState().getValue(
                        source.getSourceId().toString() + "firstChoice")).getDescription();
            }
            if (controller.choose(Outcome.Neutral, choices, game)) {
                game.informPlayers(mageObject.getName()
                        + ":  Second chosen basic land type is " + choices.getChoice());
                game.getState().setValue(mageObject.getId().toString()
                        + "secondChoice", choices.getChoice());
                choiceTwo = SubType.byDescription((String) game.getState().getValue(
                        source.getSourceId().toString() + "secondChoice")).getDescription();
                if (mageObject instanceof Permanent
                        && choiceOne != null
                        && choiceTwo != null) {
                    ((Permanent) mageObject).addInfo("Chosen Types", CardUtil
                            .addToolTipMarkTags("First chosen basic land type: " + choiceOne
                                    + "\n Second chosen basic land type: " + choiceTwo), game);
                }
                return true;
            }
        }
        return false;
    }
}
