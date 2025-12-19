
package mage.cards.e;

import mage.abilities.Abilities;
import mage.abilities.AbilitiesImpl;
import mage.abilities.Ability;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.costs.Cost;
import mage.abilities.costs.common.SacrificeTargetCost;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.effects.common.continuous.generic.ContinuousEffectBuilder;
import mage.abilities.keyword.*;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.common.FilterControlledLandPermanent;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.target.common.TargetCreaturePermanent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 *
 * @author Plopman
 */
public final class Excavator extends CardImpl {

    private static final FilterControlledLandPermanent filter = new FilterControlledLandPermanent("basic land");
    static
    {
        filter.add(SuperType.BASIC.getPredicate());
    }
   
    public Excavator(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId,setInfo,new CardType[]{CardType.ARTIFACT},"{2}");

        // {tap}, Sacrifice a basic land: Target creature gains landwalk of each of the land types of the sacrificed land until end of turn.
        Ability ability = new SimpleActivatedAbility(new ExcavatorEffect(), new TapSourceCost());
        ability.addCost(new SacrificeTargetCost(filter));
        ability.addTarget(new TargetCreaturePermanent());
        this.addAbility(ability);
    }

    private Excavator(final Excavator card) {
        super(card);
    }

    @Override
    public Excavator copy() {
        return new Excavator(this);
    }
}

class ExcavatorEffect extends ContinuousEffectBuilder {

    public ExcavatorEffect() {
        super(Duration.EndOfTurn, Outcome.AddAbility);
        addLayer(Layer.AbilityAddingRemovingEffects_6);
        setText("Target creature gains landwalk of each of the land types of the sacrificed land until end of turn");
    }

    private ExcavatorEffect(final ExcavatorEffect effect) {
        super(effect);
    }

    @Override
    public ExcavatorEffect copy() {
        return new ExcavatorEffect(this);
    }

    @Override
    public void init(Ability source, Game game) {
        super.init(source, game);
        for (Cost cost : source.getCosts()) {
            if (cost instanceof SacrificeTargetCost) {
                SacrificeTargetCost sacrificeCost = (SacrificeTargetCost) cost;
                this.gainedAbilities = new ArrayList<>();
                for (Permanent permanent : sacrificeCost.getPermanents()) {
                    if (permanent.hasSubtype(SubType.FOREST, game)) {
                        gainedAbilities.add(new ForestwalkAbility());
                    }
                    if (permanent.hasSubtype(SubType.PLAINS, game)) {
                        gainedAbilities.add(new PlainswalkAbility());
                    }
                    if (permanent.hasSubtype(SubType.ISLAND, game)) {
                        gainedAbilities.add(new IslandwalkAbility());
                    }
                    if (permanent.hasSubtype(SubType.MOUNTAIN, game)) {
                        gainedAbilities.add(new MountainwalkAbility());
                    }
                    if (permanent.hasSubtype(SubType.SWAMP, game)) {
                        gainedAbilities.add(new SwampwalkAbility());
                    }
                }
                
            }
        }
    }
}
