
package mage.cards.h;

import mage.MageItem;
import mage.abilities.Ability;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.keyword.LandwalkAbility;
import mage.abilities.mana.RedManaAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.target.common.TargetCreaturePermanent;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 *
 * @author TheElk801
 */
public final class Hammerheim extends CardImpl {

    public Hammerheim(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.LAND}, "");

        this.supertype.add(SuperType.LEGENDARY);

        // {tap}: Add {R}.
        this.addAbility(new RedManaAbility());

        // {tap}: Target creature loses all landwalk abilities until end of turn.
        Ability ability = new SimpleActivatedAbility(new HammerheimEffect(), new TapSourceCost());
        ability.addTarget(new TargetCreaturePermanent());
        this.addAbility(ability);
    }

    private Hammerheim(final Hammerheim card) {
        super(card);
    }

    @Override
    public Hammerheim copy() {
        return new Hammerheim(this);
    }
}

class HammerheimEffect extends ContinuousEffectImpl {

    HammerheimEffect() {
        super(Duration.EndOfTurn, Layer.AbilityAddingRemovingEffects_6, SubLayer.NA, Outcome.LoseAbility);
        this.staticText = "Target creature loses all landwalk abilities until end of turn.";
    }

    private HammerheimEffect(final HammerheimEffect effect) {
        super(effect);
    }

    @Override
    public HammerheimEffect copy() {
        return new HammerheimEffect(this);
    }

    @Override
    public void applyToObjects(Layer layer, SubLayer sublayer, Ability source, Game game, List<MageItem> affectedObjects) {
        for (MageItem object : affectedObjects) {
            Permanent permanent = (Permanent) object;
            List<Ability> toRemove = permanent.getAbilities().stream()
                    .filter(ab -> ab instanceof LandwalkAbility).collect(Collectors.toList());
            toRemove.forEach(ability -> permanent.removeAbility(ability, source.getSourceId(), game));
        }
    }

    @Override
    public boolean queryAffectedObjects(Layer layer, Ability source, Game game, List<MageItem> affectedObjects) {
        Permanent permanent = game.getPermanent(getTargetPointer().getFirst(game, source));
        if (permanent != null) {
            affectedObjects.add(permanent);
        }
        return !affectedObjects.isEmpty();
    }
}
