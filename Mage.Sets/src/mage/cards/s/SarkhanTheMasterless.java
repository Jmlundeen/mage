package mage.cards.s;

import mage.abilities.Ability;
import mage.abilities.LoyaltyAbility;
import mage.abilities.common.AttacksAllTriggeredAbility;
import mage.abilities.effects.ContinuousEffect;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.CreateTokenEffect;
import mage.abilities.effects.common.continuous.BecomesCreatureAllEffect;
import mage.abilities.keyword.FlyingAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.game.permanent.token.DragonToken;
import mage.game.permanent.token.custom.CreatureToken;

import java.util.UUID;

/**
 * @author TheElk801
 */
public final class SarkhanTheMasterless extends CardImpl {

    public SarkhanTheMasterless(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.PLANESWALKER}, "{3}{R}{R}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.SARKHAN);
        this.setStartingLoyalty(5);

        // Whenever a creature attacks you or a planeswalker you control, each Dragon you control deals 1 damage to that creature.
        this.addAbility(new AttacksAllTriggeredAbility(
                new SarkhanTheMasterlessDamageEffect(),
                false, StaticFilters.FILTER_PERMANENT_A_CREATURE,
                SetTargetPointer.PERMANENT, true
        ));

        // +1: Until end of turn, each planeswalker you control becomes a 4/4 red Dragon creature and gains flying.
        ContinuousEffect effect = new BecomesCreatureAllEffect(
                new CreatureToken(4, 4, "4/4 red Dragon creature and gains flying")
                        .withColor("R")
                        .withSubType(SubType.DRAGON)
                        .withAbility(FlyingAbility.getInstance()),
                null, StaticFilters.FILTER_CONTROLLED_PERMANENT_PLANESWALKER, Duration.EndOfTurn, false);
        effect.setText("Until end of turn, each planeswalker you control becomes a 4/4 red Dragon creature and gains flying");
        this.addAbility(new LoyaltyAbility(effect, 1));

        // -3: Create a 4/4 red Dragon creature token with flying.
        this.addAbility(new LoyaltyAbility(new CreateTokenEffect(new DragonToken()), -3));
    }

    private SarkhanTheMasterless(final SarkhanTheMasterless card) {
        super(card);
    }

    @Override
    public SarkhanTheMasterless copy() {
        return new SarkhanTheMasterless(this);
    }
}

class SarkhanTheMasterlessDamageEffect extends OneShotEffect {

    SarkhanTheMasterlessDamageEffect() {
        super(Outcome.Benefit);
        staticText = "each Dragon you control deals 1 damage to that creature.";
    }

    private SarkhanTheMasterlessDamageEffect(final SarkhanTheMasterlessDamageEffect effect) {
        super(effect);
    }

    @Override
    public SarkhanTheMasterlessDamageEffect copy() {
        return new SarkhanTheMasterlessDamageEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Permanent creature = game.getPermanent(getTargetPointer().getFirst(game, source));
        if (creature == null) {
            return false;
        }
        for (Permanent permanent : game.getBattlefield().getAllActivePermanents(source.getControllerId())) {
            if (permanent != null && permanent.hasSubtype(SubType.DRAGON, game)) {
                creature.damage(1, permanent.getId(), source, game);
            }
        }
        return true;
    }
}

