package mage.cards.s;

import mage.abilities.Ability;
import mage.abilities.costs.Cost;
import mage.abilities.costs.common.SacrificeTargetCost;
import mage.abilities.effects.Effect;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.abilities.mana.providers.ManaTypeProvider;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.ManaType;
import mage.filter.StaticFilters;
import mage.filter.common.FilterControlledLandPermanent;
import mage.filter.common.FilterControlledPermanent;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.util.PermanentUtil;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * @author escplan9 (Derek Monturo - dmontur1 at gmail dot com)
 */
public final class SquanderedResources extends CardImpl {

    private static final FilterControlledPermanent filter = new FilterControlledLandPermanent("a land");

    public SquanderedResources(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ENCHANTMENT}, "{B}{G}");

        // Sacrifice a land: Add one mana of any type the sacrificed land could produce.
        this.addAbility(new ComposedManaAbilityBuilder()
                .cost(new SacrificeTargetCost(filter))
                .addChoice(SquanderedResourceManaTypeProvider.instance, 1)
                .ruleText("Add one mana of any type the sacrificed land could produce")
                .build()

        );
    }

    private SquanderedResources(final SquanderedResources card) {
        super(card);
    }

    @Override
    public SquanderedResources copy() {
        return new SquanderedResources(this);
    }
}

enum SquanderedResourceManaTypeProvider implements ManaTypeProvider {
    instance;

    @Override
    public Set<ManaType> getManaTypes(Game game, Ability source, Effect effect) {
        Set<ManaType> types = new HashSet<>();
        if (game == null) {
            return types;
        }
        for (Cost cost : source.getCosts()) {
            if (cost instanceof SacrificeTargetCost && !((SacrificeTargetCost) cost).getPermanents().isEmpty()) {
                Permanent land = ((SacrificeTargetCost) cost).getPermanents().getFirst();
                if (land != null) {
                    types.addAll(PermanentUtil.getProducibleMana(land, game));
                    break;
                }
            }
        }
        if (types.isEmpty() && game.inCheckPlayableState()) {
            // add color combinations of available mana
            for (Permanent land : game.getBattlefield().getAllActivePermanents(StaticFilters.FILTER_LAND, source.getControllerId(), game)) {
                types.addAll(PermanentUtil.getProducibleMana(land, game));
            }
        }
        return types;
    }
}
