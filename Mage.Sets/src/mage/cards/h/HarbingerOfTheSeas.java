package mage.cards.h;

import mage.MageInt;
import mage.MageItem;
import mage.abilities.Ability;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.mana.BlueManaAbility;
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
 * @author Susucr
 */
public final class HarbingerOfTheSeas extends CardImpl {

    public HarbingerOfTheSeas(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{1}{U}{U}");

        this.subtype.add(SubType.MERFOLK);
        this.subtype.add(SubType.WIZARD);
        this.power = new MageInt(2);
        this.toughness = new MageInt(2);

        // Nonbasic lands are Islands.
        this.addAbility(new SimpleStaticAbility(new HarbingerOfTheSeasEffect()));
    }

    private HarbingerOfTheSeas(final HarbingerOfTheSeas card) {
        super(card);
    }

    @Override
    public HarbingerOfTheSeas copy() {
        return new HarbingerOfTheSeas(this);
    }
}

// Very similar to Magus of the Moon
class HarbingerOfTheSeasEffect extends ContinuousEffectImpl {

    private static final FilterLandPermanent filter = new FilterLandPermanent();

    static {
        filter.add(Predicates.not(SuperType.BASIC.getPredicate()));
    }

    HarbingerOfTheSeasEffect() {
        super(Duration.WhileOnBattlefield, Layer.TypeChangingEffects_4, SubLayer.NA, Outcome.Detriment);
        this.staticText = "Nonbasic lands are Islands";
        dependendToTypes.add(DependencyType.BecomeNonbasicLand);
        dependencyTypes.add(DependencyType.BecomeIsland);
    }

    private HarbingerOfTheSeasEffect(final HarbingerOfTheSeasEffect effect) {
        super(effect);
    }

    @Override
    public HarbingerOfTheSeasEffect copy() {
        return new HarbingerOfTheSeasEffect(this);
    }

    @Override
    public void applyToObjects(Layer layer, SubLayer sublayer, Ability source, Game game, List<MageItem> affectedObjects) {
        for (MageItem object : affectedObjects) {
            Permanent permanent = (Permanent) object;
            // 305.7 Note that this doesn't remove any abilities that were granted to the land by other effects
            // So the ability removing has to be done before Layer 6
            permanent.removeAllAbilities(source.getSourceId(), game);
            permanent.removeAllSubTypes(game, SubTypeSet.NonBasicLandType);
            permanent.addSubType(game, SubType.ISLAND);
            // Islands have the blue mana ability intrinsically so the ability must be added in this layer
            permanent.addAbility(new BlueManaAbility(), source.getSourceId(), game);
        }
    }

    @Override
    public boolean queryAffectedObjects(Layer layer, Ability source, Game game, List<MageItem> affectedObjects) {
        affectedObjects.addAll(game.getBattlefield().getActivePermanents(filter, source.getControllerId(), game));
        return !affectedObjects.isEmpty();
    }
}
