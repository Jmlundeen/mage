package mage.cards.e;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.common.TurnFaceUpAbility;
import mage.abilities.effects.common.cost.CostModificationEffectImpl;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.util.CardUtil;

import java.util.UUID;

/**
 * @author xenohedron
 */
public final class ExiledDoomsayer extends CardImpl {

    public ExiledDoomsayer(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{1}{W}");
        
        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.CLERIC);
        this.power = new MageInt(1);
        this.toughness = new MageInt(2);

        // All morph costs cost {2} more.
        this.addAbility(new SimpleStaticAbility(new ExiledDoomsayerEffect()));

    }

    private ExiledDoomsayer(final ExiledDoomsayer card) {
        super(card);
    }

    @Override
    public ExiledDoomsayer copy() {
        return new ExiledDoomsayer(this);
    }
}

class ExiledDoomsayerEffect extends CostModificationEffectImpl {

    ExiledDoomsayerEffect() {
        super(Duration.WhileOnBattlefield, Outcome.Neutral, CostModificationType.INCREASE_COST);
        staticText = "All morph costs cost {2} more. <i>(This doesn't affect the cost to cast creature spells face down.)</i>";
    }

    private ExiledDoomsayerEffect(final ExiledDoomsayerEffect effect) {
        super(effect);
    }

    @Override
    public boolean apply(Game game, Ability source, Ability abilityToModify) {
        CardUtil.increaseCost(abilityToModify, 2);
        return true;
    }

    @Override
    public boolean applies(Ability abilityToModify, Ability source, Game game) {
        if (!(abilityToModify instanceof TurnFaceUpAbility)) {
            return false;
        }
        Permanent permanent = game.getPermanent(abilityToModify.getSourceId());
        return permanent != null && permanent.isMorphed();
    }

    @Override
    public ExiledDoomsayerEffect copy() {
        return new ExiledDoomsayerEffect(this);
    }
}
