package mage.cards.a;

import mage.abilities.Ability;
import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.effects.ContinuousEffect;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.ExileTargetEffect;
import mage.abilities.effects.common.continuous.GainControlTargetEffect;
import mage.abilities.triggers.BeginningOfUpkeepTriggeredAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.target.TargetPermanent;
import mage.target.targetpointer.FixedTarget;

import java.util.UUID;

/**
 *
 * @author LevelX2
 */
public final class ActOfAuthority extends CardImpl {

    public ActOfAuthority(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ENCHANTMENT}, "{1}{W}{W}");

        // When Act of Authority enters the battlefield, you may exile target artifact or enchantment.
        Ability ability = new EntersBattlefieldTriggeredAbility(new ExileTargetEffect(), true);
        ability.addTarget(new TargetPermanent(StaticFilters.FILTER_PERMANENT_ARTIFACT_OR_ENCHANTMENT));
        this.addAbility(ability);
        // At the beginning of your upkeep, you may exile target artifact or enchantment. If you do, its controller gains control of Act of Authority.
        ability = new BeginningOfUpkeepTriggeredAbility(new ActOfAuthorityEffect(), true);
        ability.addTarget(new TargetPermanent(StaticFilters.FILTER_PERMANENT_ARTIFACT_OR_ENCHANTMENT));
        this.addAbility(ability);
    }

    private ActOfAuthority(final ActOfAuthority card) {
        super(card);
    }

    @Override
    public ActOfAuthority copy() {
        return new ActOfAuthority(this);
    }
}

class ActOfAuthorityEffect extends OneShotEffect {

    ActOfAuthorityEffect() {
        super(Outcome.Exile);
        this.staticText = "you may exile target artifact or enchantment. If you do, its controller gains control of {this}";
    }

    private ActOfAuthorityEffect(final ActOfAuthorityEffect effect) {
        super(effect);
    }

    @Override
    public ActOfAuthorityEffect copy() {
        return new ActOfAuthorityEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Permanent targetPermanent = game.getPermanent(getTargetPointer().getFirst(game, source));
        if (targetPermanent == null) {
            return false;
        }

        ExileTargetEffect exileTargetEffect = new ExileTargetEffect();
        if (!exileTargetEffect.apply(game, source)) {
            return false;
        }

        Permanent sourcePermanent = source.getSourcePermanentIfItStillExists(game);
        if (sourcePermanent == null) { return true; }

        ContinuousEffect effect = new GainControlTargetEffect(Duration.Custom, targetPermanent.getControllerId());
        effect.setTargetPointer(new FixedTarget(sourcePermanent, game));
        game.addEffect(effect, source);
        return true;
    }
}
