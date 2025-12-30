
package mage.cards.x;

import mage.MageInt;
import mage.MageItem;
import mage.abilities.Ability;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.common.FilterArtifactPermanent;
import mage.filter.predicate.Predicates;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.target.TargetPermanent;

import java.util.List;
import java.util.UUID;

/**
 * @author MarcoMarin
 */
public final class XenicPoltergeist extends CardImpl {

    private static final FilterArtifactPermanent filter = new FilterArtifactPermanent();

    static {
        filter.add(Predicates.not(CardType.CREATURE.getPredicate()));
    }

    public XenicPoltergeist(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{1}{B}{B}");
        this.subtype.add(SubType.SPIRIT);
        this.power = new MageInt(1);
        this.toughness = new MageInt(1);

        // {tap}: Until your next upkeep, target noncreature artifact becomes an artifact creature with power and toughness each equal to its converted mana cost.
        Ability ability = new SimpleActivatedAbility(new XenicPoltergeistEffect(), new TapSourceCost());
        ability.addTarget(new TargetPermanent(filter));
        this.addAbility(ability);

    }

    private XenicPoltergeist(final XenicPoltergeist card) {
        super(card);
    }

    @Override
    public XenicPoltergeist copy() {
        return new XenicPoltergeist(this);
    }
}

class XenicPoltergeistEffect extends ContinuousEffectImpl {

    private static final FilterArtifactPermanent filter = new FilterArtifactPermanent();

    static {
        filter.add(Predicates.not(CardType.CREATURE.getPredicate()));
    }

    public XenicPoltergeistEffect() {
        super(Duration.Custom, Outcome.BecomeCreature);
        staticText = "Until your next upkeep, target noncreature artifact becomes an artifact creature with power and toughness each equal to its mana value";
    }

    private XenicPoltergeistEffect(final XenicPoltergeistEffect effect) {
        super(effect);
    }

    @Override
    public XenicPoltergeistEffect copy() {
        return new XenicPoltergeistEffect(this);
    }

    @Override
    public boolean isInactive(Ability source, Game game) {
        if (game.getPhase().getStep().getType() == PhaseStep.UPKEEP) {
            if (game.isActivePlayer(source.getControllerId())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void applyToObjects(Layer layer, SubLayer sublayer, Ability source, Game game, List<MageItem> affectedObjects) {
        for (MageItem object : affectedObjects) {
            Permanent permanent = (Permanent) object;
            switch (layer) {
                case TypeChangingEffects_4:
                    permanent.addCardType(game, CardType.ARTIFACT);
                    permanent.addCardType(game, CardType.CREATURE);
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
    public boolean queryAffectedObjects(Layer layer, Ability source, Game game, List<MageItem> affectedObjects) {
        UUID permanentId = getTargetPointer().getFirst(game, source);
        Permanent permanent = game.getPermanentOrLKIBattlefield(permanentId);
        affectedObjects.add(permanent);
        return true;
    }

    @Override
    public boolean hasLayer(Layer layer) {
        return layer == Layer.PTChangingEffects_7
                || layer == Layer.TypeChangingEffects_4;
    }

    @Override
    public boolean hasSubLayer(SubLayer sublayer) {
        return sublayer == SubLayer.SetPT_7b
                || sublayer == SubLayer.NA;
    }
}
