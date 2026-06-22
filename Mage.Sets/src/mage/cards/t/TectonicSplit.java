package mage.cards.t;

import mage.abilities.Ability;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.costs.Cost;
import mage.abilities.costs.CostImpl;
import mage.abilities.effects.ContinuousEffect;
import mage.abilities.effects.common.continuous.generic.GenericContinuousEffect;
import mage.abilities.keyword.HexproofAbility;
import mage.abilities.mana.AnyColorManaAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.Zone;
import mage.filter.FilterPermanent;
import mage.filter.StaticFilters;
import mage.filter.StaticTypedFilters;
import mage.filter.common.FilterControlledLandPermanent;
import mage.filter.predicate.permanent.CanBeSacrificedPredicate;
import mage.game.Game;
import mage.players.Player;
import mage.target.common.TargetSacrifice;

import java.util.Optional;
import java.util.UUID;

/**
 * @author TheElk801
 */
public final class TectonicSplit extends CardImpl {

    public TectonicSplit(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ENCHANTMENT}, "{4}{G}{G}");

        // As an additional cost to cast this spell, sacrifice half the lands you control, rounded up.
        this.getSpellAbility().addCost(new TectonicSplitCost());

        // Hexproof
        this.addAbility(HexproofAbility.getInstance());

        // Lands you control have "{T}: Add three mana of any one color."
        ContinuousEffect effect = new GenericContinuousEffect(Outcome.AddAbility, StaticTypedFilters.LAND_YOU_CONTROL, Zone.BATTLEFIELD)
                .withGainedAbilities(new AnyColorManaAbility(3))
                .setText("lands you control have \"{T}: Add three mana of any one color.\"");
        this.addAbility(new SimpleStaticAbility(effect));
    }

    private TectonicSplit(final TectonicSplit card) {
        super(card);
    }

    @Override
    public TectonicSplit copy() {
        return new TectonicSplit(this);
    }
}

class TectonicSplitCost extends CostImpl {

    private static final FilterPermanent filter = new FilterControlledLandPermanent();

    static {
        filter.add(CanBeSacrificedPredicate.instance);
    }

    TectonicSplitCost() {
        super();
        setText("sacrifice half the lands you control, rounded up");
    }

    private TectonicSplitCost(final TectonicSplitCost cost) {
        super(cost);
    }

    @Override
    public TectonicSplitCost copy() {
        return new TectonicSplitCost(this);
    }

    @Override
    public boolean canPay(Ability ability, Ability source, UUID controllerId, Game game) {
        return 2 * game.getBattlefield().count(filter, controllerId, source, game)
                >= game.getBattlefield().count(StaticFilters.FILTER_CONTROLLED_PERMANENT_LAND, controllerId, source, game);
    }

    @Override
    public boolean pay(Ability ability, Game game, Ability source, UUID controllerId, boolean noMana, Cost costToPay) {
        Player player = game.getPlayer(controllerId);
        if (player == null) {
            paid = false;
            return paid;
        }
        int count = (1 + game.getBattlefield().count(StaticFilters.FILTER_CONTROLLED_PERMANENT_LAND, controllerId, source, game)) / 2;
        if (count == 0) {
            paid = true;
            return paid;
        }
        TargetSacrifice target = new TargetSacrifice(count, StaticFilters.FILTER_LAND);
        player.choose(Outcome.Sacrifice, target, source, game);
        for (UUID targetId : target.getTargets()) {
            Optional.ofNullable(targetId)
                    .map(game::getPermanent)
                    .ifPresent(permanent -> permanent.sacrifice(source, game));
        }
        paid = true;
        return paid;
    }
}
