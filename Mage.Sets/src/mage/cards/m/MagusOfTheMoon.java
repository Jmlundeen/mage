package mage.cards.m;

import mage.MageInt;
import mage.MageItem;
import mage.abilities.Ability;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.mana.RedManaAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.common.FilterLandPermanent;
import mage.filter.predicate.Predicates;
import mage.game.Game;
import mage.game.permanent.Permanent;

import java.util.List;
import java.util.UUID;

/**
 * @author LevelX2
 */
public final class MagusOfTheMoon extends CardImpl {

    public MagusOfTheMoon(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{2}{R}");
        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.WIZARD);

        this.power = new MageInt(2);
        this.toughness = new MageInt(2);

        // Nonbasic lands are Mountains.
        this.addAbility(new SimpleStaticAbility(new MagusOfTheMoonEffect()));
    }

    private MagusOfTheMoon(final MagusOfTheMoon card) {
        super(card);
    }

    @Override
    public MagusOfTheMoon copy() {
        return new MagusOfTheMoon(this);
    }

}

class MagusOfTheMoonEffect extends ContinuousEffectImpl {

    private static final FilterLandPermanent filter = new FilterLandPermanent();

    static {
        filter.add(Predicates.not(SuperType.BASIC.getPredicate()));
    }

    MagusOfTheMoonEffect() {
        super(Duration.WhileOnBattlefield, Layer.TypeChangingEffects_4, SubLayer.NA, Outcome.Detriment);
        this.staticText = "Nonbasic lands are Mountains";
        dependendToTypes.add(DependencyType.BecomeNonbasicLand);
        dependencyTypes.add(DependencyType.BecomeMountain);
    }

    private MagusOfTheMoonEffect(final MagusOfTheMoonEffect effect) {
        super(effect);
    }

    @Override
    public MagusOfTheMoonEffect copy() {
        return new MagusOfTheMoonEffect(this);
    }

    @Override
    public void applyToObjects(Layer layer, SubLayer sublayer, Ability source, Game game, List<MageItem> affectedObjects) {
        for (MageItem object : affectedObjects) {
            Permanent land = (Permanent) object;
            // 305.7 Note that this doesn't remove any abilities that were granted to the land by other effects
            // So the ability removing has to be done before Layer 6
            land.removeAllAbilities(source.getSourceId(), game);
            land.removeAllSubTypes(game, SubTypeSet.NonBasicLandType);
            land.addSubType(game, SubType.MOUNTAIN);
            // Mountains have the red mana ability intrinsically so the ability must be added in this layer
            land.addAbility(new RedManaAbility(), source.getSourceId(), game);
        }
    }

    @Override
    public boolean queryAffectedObjects(Layer layer, Ability source, Game game, List<MageItem> affectedObjects) {
        affectedObjects.addAll(game.getBattlefield().getActivePermanents(filter, source.getControllerId(), game));
        return !affectedObjects.isEmpty();
    }

    @Override
    public boolean hasLayer(Layer layer) {
        return layer == Layer.TypeChangingEffects_4;
    }
}
