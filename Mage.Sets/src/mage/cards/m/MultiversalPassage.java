package mage.cards.m;

import mage.MageItem;
import mage.abilities.Ability;
import mage.abilities.common.AsEntersBattlefieldAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.costs.common.PayLifeCost;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.effects.common.ChooseBasicLandTypeEffect;
import mage.abilities.effects.common.TapSourceUnlessPaysEffect;
import mage.abilities.mana.*;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.game.Game;
import mage.game.permanent.Permanent;

import java.util.List;
import java.util.UUID;

/**
 *
 * @author Jmlundeen
 */
public final class MultiversalPassage extends CardImpl {

    public MultiversalPassage(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.LAND}, "");
        

        // As this land enters, choose a basic land type. Then you may pay 2 life. If you don't, it enters tapped.
        Ability ability = new AsEntersBattlefieldAbility(new ChooseBasicLandTypeEffect(Outcome.Benefit));
        ability.addEffect(new TapSourceUnlessPaysEffect(new PayLifeCost(2)));
        this.addAbility(ability);

        // This land is the chosen type.
        this.addAbility(new SimpleStaticAbility(new MultiversalPassagePassageEffect()));
    }

    private MultiversalPassage(final MultiversalPassage card) {
        super(card);
    }

    @Override
    public MultiversalPassage copy() {
        return new MultiversalPassage(this);
    }
}
class MultiversalPassagePassageEffect extends ContinuousEffectImpl {

    MultiversalPassagePassageEffect() {
        super(Duration.WhileOnBattlefield, Layer.TypeChangingEffects_4, SubLayer.NA, Outcome.Neutral);
        staticText = "This land is the chosen type.";
    }

    private MultiversalPassagePassageEffect(final MultiversalPassagePassageEffect effect) {
        super(effect);
    }

    @Override
    public MultiversalPassagePassageEffect copy() {
        return new MultiversalPassagePassageEffect(this);
    }

    @Override
    public void init(Ability source, Game game) {
        super.init(source, game);
        SubType choice = SubType.byDescription((String) game.getState().getValue(source.getSourceId().toString() + ChooseBasicLandTypeEffect.VALUE_KEY));
        if (choice == null) {
            discard();
        }
    }

    @Override
    public void applyToObjects(Layer layer, SubLayer sublayer, Ability source, Game game, List<MageItem> affectedObjects) {
        SubType choice = SubType.byDescription((String) game.getState().getValue(source.getSourceId().toString() + ChooseBasicLandTypeEffect.VALUE_KEY));
        assert choice != null;
        Ability ability;
        switch (choice) {
            case PLAINS:
                ability = new WhiteManaAbility();
                break;
            case ISLAND:
                ability = new BlueManaAbility();
                break;
            case SWAMP:
                ability = new BlackManaAbility();
                break;
            case MOUNTAIN:
                ability = new RedManaAbility();
                break;
            case FOREST:
                ability = new GreenManaAbility();
                break;
            default:
                ability = null;
        }
        for (MageItem object : affectedObjects) {
            Permanent land = (Permanent) object;
            land.addSubType(game, choice);
            land.addAbility(ability, source.getSourceId(), game);
        }
    }

    @Override
    public boolean queryAffectedObjects(Layer layer, Ability source, Game game, List<MageItem> affectedObjects) {
        SubType choice = SubType.byDescription((String) game.getState().getValue(source.getSourceId().toString() + ChooseBasicLandTypeEffect.VALUE_KEY));
        if (choice == null) {
            return false;
        }
        Permanent land = game.getPermanent(source.getSourceId());
        if (land == null || land.hasSubtype(choice, game)) {
            return false;
        }
        affectedObjects.add(land);
        return true;
    }
}
