
package mage.cards.g;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.continuous.BoostAllEffect;
import mage.abilities.keyword.FlashAbility;
import mage.abilities.keyword.ReachAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.StaticFilters;
import mage.filter.common.FilterCreaturePermanent;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.target.TargetPlayer;

import java.util.UUID;

/**
 * @author fireshoes
 */
public final class GreatOakGuardian extends CardImpl {

    private static final FilterCreaturePermanent filter = new FilterCreaturePermanent("creatures target player controls");

    static {
        filter.add(TargetController.SOURCE_TARGETS.getControllerPredicate());
    }

    public GreatOakGuardian(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{5}{G}");
        this.subtype.add(SubType.TREEFOLK);
        this.power = new MageInt(4);
        this.toughness = new MageInt(5);

        // Flash
        this.addAbility(FlashAbility.getInstance());

        // Reach
        this.addAbility(ReachAbility.getInstance());

        // When Great Oak Guardian enters the battlefield, creatures target player controls get +2/+2 until end of turn. Untap them.
        Ability ability = new EntersBattlefieldTriggeredAbility(
                new BoostAllEffect(2, 2, Duration.EndOfTurn, filter, false), false);
        ability.addEffect(new GreatOakGuardianUntapEffect());
        ability.addTarget(new TargetPlayer());
        this.addAbility(ability);
    }

    private GreatOakGuardian(final GreatOakGuardian card) {
        super(card);
    }

    @Override
    public GreatOakGuardian copy() {
        return new GreatOakGuardian(this);
    }
}

class GreatOakGuardianUntapEffect extends OneShotEffect {

    GreatOakGuardianUntapEffect() {
        super(Outcome.Benefit);
        this.staticText = "untap them";
    }

    private GreatOakGuardianUntapEffect(final GreatOakGuardianUntapEffect effect) {
        super(effect);
    }

    @Override
    public GreatOakGuardianUntapEffect copy() {
        return new GreatOakGuardianUntapEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player targetPlayer = game.getPlayer(source.getControllerId());
        if (targetPlayer != null) {
            for (Permanent permanent : game.getBattlefield().getAllActivePermanents(StaticFilters.FILTER_PERMANENT_CREATURE, targetPlayer.getId(), game)) {
                permanent.untap(game);
            }
            return true;
        }
        return false;
    }
}
