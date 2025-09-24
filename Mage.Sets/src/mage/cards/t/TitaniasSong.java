
package mage.cards.t;

import mage.MageItem;
import mage.abilities.Ability;
import mage.abilities.common.LeavesBattlefieldTriggeredAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.common.FilterArtifactPermanent;
import mage.filter.predicate.Predicates;
import mage.game.Game;
import mage.game.permanent.Permanent;

import java.util.List;
import java.util.UUID;

/**
 *
 * @author MarcoMarin
 */
public final class TitaniasSong extends CardImpl {

    public TitaniasSong(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ENCHANTMENT}, "{3}{G}");

        // Each noncreature artifact loses all abilities and becomes an artifact creature with power and toughness each equal to its converted mana cost. If Titania's Song leaves the battlefield, this effect continues until end of turn.
        this.addAbility(new SimpleStaticAbility(new TitaniasSongEffect(Duration.WhileOnBattlefield)));
        this.addAbility(new LeavesBattlefieldTriggeredAbility(new TitaniasSongEffect(Duration.EndOfTurn), false));
    }

    private TitaniasSong(final TitaniasSong card) {
        super(card);
    }

    @Override
    public TitaniasSong copy() {
        return new TitaniasSong(this);
    }
}

class TitaniasSongEffect extends ContinuousEffectImpl {

    private static final FilterArtifactPermanent filter = new FilterArtifactPermanent();

    static {
        filter.add(Predicates.not(CardType.CREATURE.getPredicate()));
    }

    public TitaniasSongEffect(Duration duration) {
        super(duration, Outcome.BecomeCreature);
        staticText = "Each noncreature artifact loses its abilities and is an artifact creature with power and toughness each equal to its mana value";
        this.dependencyTypes.add(DependencyType.BecomeCreature);
    }

    private TitaniasSongEffect(final TitaniasSongEffect effect) {
        super(effect);
    }

    @Override
    public TitaniasSongEffect copy() {
        return new TitaniasSongEffect(this);
    }

    @Override
    public void applyToObjects(Layer layer, SubLayer sublayer, Ability source, Game game, List<MageItem> affectedObjects) {
        for (MageItem object : affectedObjects) {
            Permanent permanent = (Permanent) object;
            switch (layer) {
                case TypeChangingEffects_4:
                    permanent.addCardType(game, CardType.CREATURE);
                    break;
                case AbilityAddingRemovingEffects_6:
                    permanent.removeAllAbilities(source.getSourceId(), game);
                    break;
                case PTChangingEffects_7:
                    if (sublayer == SubLayer.SetPT_7b) {
                        int manaCost = permanent.getManaValue();
                        permanent.getPower().setModifiedBaseValue(manaCost);
                        permanent.getToughness().setModifiedBaseValue(manaCost);
                    }
            }
        }
    }

    @Override
    public boolean queryAffectedObjects(Ability source, Game game, List<MageItem> affectedObjects) {
        if (!source.getAffectedObjects().isEmpty()) {
            affectedObjects.addAll(source.getAffectedObjects());
        } else {
            for (Permanent permanent : game.getBattlefield().getActivePermanents(filter, source.getControllerId(), source, game)) {
                affectedObjects.add(permanent);
                source.getAffectedObjects().add(permanent);
            }
        }
        return !affectedObjects.isEmpty();
    }

    @Override
    public boolean hasLayer(Layer layer) {
        return layer == Layer.PTChangingEffects_7 || layer == Layer.AbilityAddingRemovingEffects_6 || layer == Layer.TypeChangingEffects_4;
    }

    @Override
    public boolean hasSubLayer(SubLayer sublayer) {
        return sublayer == SubLayer.SetPT_7b || sublayer == SubLayer.NA;
    }
}
