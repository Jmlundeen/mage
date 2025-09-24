package mage.cards.r;

import mage.MageInt;
import mage.MageItem;
import mage.abilities.Ability;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.keyword.VigilanceAbility;
import mage.abilities.mana.BasicManaAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.common.FilterControlledCreaturePermanent;
import mage.game.Game;
import mage.game.permanent.Permanent;

import java.util.*;

/**
 * @author PurpleCrowbar
 */
public final class RobaranMercenaries extends CardImpl {

    public RobaranMercenaries(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{3}{W}");
        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.MERCENARY);

        this.power = new MageInt(3);
        this.toughness = new MageInt(4);

        // Vigilance
        this.addAbility(VigilanceAbility.getInstance());

        // Robaran Mercenaries has all activated abilties of all legendary creatures you control.
        this.addAbility(new SimpleStaticAbility(new RobaranMercenariesEffect()));
    }

    private RobaranMercenaries(final RobaranMercenaries card) {
        super(card);
    }

    @Override
    public RobaranMercenaries copy() {
        return new RobaranMercenaries(this);
    }
}

class RobaranMercenariesEffect extends ContinuousEffectImpl {

    private static final FilterControlledCreaturePermanent filter = new FilterControlledCreaturePermanent();

    static {
        filter.add(SuperType.LEGENDARY.getPredicate());
    }

    RobaranMercenariesEffect() {
        super(Duration.WhileOnBattlefield, Layer.AbilityAddingRemovingEffects_6, SubLayer.NA, Outcome.AddAbility);
        staticText = "{this} has all activated abilities of all legendary creatures you control.";
        this.addDependencyType(DependencyType.AddingAbility);
    }

    private RobaranMercenariesEffect(final RobaranMercenariesEffect effect) {
        super(effect);
    }

    @Override
    public void applyToObjects(Layer layer, SubLayer sublayer, Ability source, Game game, List<MageItem> affectedObjects) {
        List<Ability> abilities = new ArrayList<>();
        for (Permanent creature : game.getBattlefield().getActivePermanents(filter, source.getControllerId(), source, game)) {
            for (Ability ability : creature.getAbilities()) {
                if (ability.isActivatedAbility()) {
                    abilities.add(ability);
                }
            }
        }
        for (MageItem object : affectedObjects) {
            for (Ability ability : abilities) {
                // optimization to disallow the adding of duplicate, unnecessary basic mana abilities
                if (ability instanceof BasicManaAbility
                        && ((Permanent) object).getAbilities(game)
                        .stream()
                        .anyMatch(ability.getClass()::isInstance)) {
                    continue;
                }
                ((Permanent) object).addAbility(ability, source.getSourceId(), game, true);
            }
        }
    }

    @Override
    public boolean queryAffectedObjects(Layer layer, Ability source, Game game, List<MageItem> affectedObjects) {
        Permanent permanent = game.getPermanent(source.getSourceId());
        if (permanent != null) {
            affectedObjects.add(permanent);
            return true;
        }
        return false;
    }

    @Override
    public RobaranMercenariesEffect copy() {
        return new RobaranMercenariesEffect(this);
    }
}
