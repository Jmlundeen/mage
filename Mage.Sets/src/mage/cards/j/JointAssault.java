
package mage.cards.j;

import mage.MageItem;
import mage.MageObjectReference;
import mage.abilities.Ability;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.target.common.TargetCreaturePermanent;

import java.util.List;
import java.util.UUID;

/**
 * @author noxx
 */
public final class JointAssault extends CardImpl {

    public JointAssault(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId,setInfo,new CardType[]{CardType.INSTANT},"{G}");

        // Target creature gets +2/+2 until end of turn. If it's paired with a creature, that creature also gets +2/+2 until end of turn.
        this.getSpellAbility().addEffect(new JointAssaultBoostTargetEffect(2, 2, Duration.EndOfTurn));
        this.getSpellAbility().addTarget(new TargetCreaturePermanent());
    }

    private JointAssault(final JointAssault card) {
        super(card);
    }

    @Override
    public JointAssault copy() {
        return new JointAssault(this);
    }
}

class JointAssaultBoostTargetEffect extends ContinuousEffectImpl {

    private final int power;
    private final int toughness;
    private MageObjectReference paired;

    public JointAssaultBoostTargetEffect(int power, int toughness, Duration duration) {
        super(duration, Layer.PTChangingEffects_7, SubLayer.ModifyPT_7c, Outcome.BoostCreature);
        this.power = power;
        this.toughness = toughness;
        staticText = "Target creature gets +2/+2 until end of turn. If it's paired with a creature, that creature also gets +2/+2 until end of turn";
    }

    private JointAssaultBoostTargetEffect(final JointAssaultBoostTargetEffect effect) {
        super(effect);
        this.power = effect.power;
        this.toughness = effect.toughness;
        this.paired = effect.paired;
    }

    @Override
    public JointAssaultBoostTargetEffect copy() {
        return new JointAssaultBoostTargetEffect(this);
    }

    @Override
    public void init(Ability source, Game game) {
        super.init(source, game);
        UUID permanentId = getTargetPointer().getFirst(game, source);
        Permanent target = game.getPermanent(permanentId);
        if (target != null) {
            if (target.getPairedCard() != null) {
                this.paired = target.getPairedCard();
            }
        }
    }

    @Override
    public void applyToObjects(Layer layer, SubLayer sublayer, Ability source, Game game, List<MageItem> affectedObjects) {
        for (MageItem object : affectedObjects) {
            Permanent permanent = (Permanent) object;
            permanent.addPower(power);
            permanent.addToughness(toughness);
        }
    }

    @Override
    public boolean queryAffectedObjects(Layer layer, Ability source, Game game, List<MageItem> affectedObjects) {
        Permanent target = game.getPermanent(getTargetPointer().getFirst(game, source));
        if (target != null) {
            affectedObjects.add(target);
        }
        if (this.paired != null) {
            Permanent pairedPermanent = this.paired.getPermanent(game);
            if (pairedPermanent != null) {
                affectedObjects.add(pairedPermanent);
            }
        }
        return !affectedObjects.isEmpty();
    }
}
