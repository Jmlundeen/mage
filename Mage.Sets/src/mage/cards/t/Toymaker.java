
package mage.cards.t;

import mage.MageInt;
import mage.MageItem;
import mage.abilities.Ability;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.costs.common.DiscardCardCost;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.costs.mana.ManaCostsImpl;
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
 *
 * @author TheElk801
 */
public final class Toymaker extends CardImpl {

    private static final FilterArtifactPermanent filter = new FilterArtifactPermanent("noncreature artifact");

    static {
        filter.add(Predicates.not(CardType.CREATURE.getPredicate()));
    }

    public Toymaker(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT, CardType.CREATURE}, "{2}");

        this.subtype.add(SubType.SPELLSHAPER);
        this.power = new MageInt(1);
        this.toughness = new MageInt(1);

        // {1}, {tap}, Discard a card: Target noncreature artifact becomes an artifact creature with power and toughness each equal to its converted mana cost until end of turn.
        Ability ability = new SimpleActivatedAbility(new ToymakerEffect(), new ManaCostsImpl<>("{1}"));
        ability.addCost(new TapSourceCost());
        ability.addCost(new DiscardCardCost());
        ability.addTarget(new TargetPermanent(filter));
        this.addAbility(ability);
    }

    private Toymaker(final Toymaker card) {
        super(card);
    }

    @Override
    public Toymaker copy() {
        return new Toymaker(this);
    }
}

class ToymakerEffect extends ContinuousEffectImpl {

    ToymakerEffect() {
        super(Duration.EndOfTurn, Outcome.BecomeCreature);
        staticText = "Target noncreature artifact becomes an artifact creature with power and toughness each equal to its mana value until end of turn";
        this.dependencyTypes.add(DependencyType.BecomeCreature);
    }

    private ToymakerEffect(final ToymakerEffect effect) {
        super(effect);
    }

    @Override
    public ToymakerEffect copy() {
        return new ToymakerEffect(this);
    }

    @Override
    public void applyToObjects(Layer layer, SubLayer sublayer, Ability source, Game game, List<MageItem> affectedObjects) {
        for (MageItem object : affectedObjects) {
            Permanent artifact = (Permanent) object;
            switch (layer) {
                case TypeChangingEffects_4:
                    artifact.addCardType(game, CardType.ARTIFACT);
                    artifact.addCardType(game, CardType.CREATURE);
                    break;

                case PTChangingEffects_7:
                    if (sublayer == SubLayer.SetPT_7b) {
                        int cmc = artifact.getManaValue();
                        artifact.getPower().setModifiedBaseValue(cmc);
                        artifact.getToughness().setModifiedBaseValue(cmc);
                    }
            }
        }
    }

    @Override
    public boolean queryAffectedObjects(Layer layer, Ability source, Game game, List<MageItem> affectedObjects) {
        Permanent artifact = game.getPermanent(this.getTargetPointer().getFirst(game, source));
        if (artifact != null) {
            affectedObjects.add(artifact);
        }
        return !affectedObjects.isEmpty();
    }

    @Override
    public boolean hasLayer(Layer layer) {
        return layer == Layer.PTChangingEffects_7 || layer == Layer.TypeChangingEffects_4;
    }

    @Override
    public boolean hasSubLayer(SubLayer sublayer) {
        return sublayer == SubLayer.SetPT_7b || sublayer == SubLayer.NA;
    }
}
