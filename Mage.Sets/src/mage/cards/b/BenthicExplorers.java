package mage.cards.b;

import mage.MageInt;
import mage.abilities.Abilities;
import mage.abilities.Ability;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.costs.common.UntapTargetCost;
import mage.abilities.effects.Effect;
import mage.abilities.mana.ActivatedManaAbilityImpl;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.abilities.mana.providers.ManaTypeProvider;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.FilterPermanent;
import mage.filter.common.FilterLandPermanent;
import mage.filter.predicate.permanent.TappedPredicate;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.target.TargetPermanent;

import java.util.*;

/**
 * @author jeffwadsworth
 */
public final class BenthicExplorers extends CardImpl {

    private static final FilterPermanent filter = new FilterLandPermanent("tapped land an opponent controls");

    static {
        filter.add(TargetController.OPPONENT.getControllerPredicate());
        filter.add(TappedPredicate.TAPPED);
    }

    public BenthicExplorers(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{3}{U}");

        this.subtype.add(SubType.MERFOLK);
        this.subtype.add(SubType.SCOUT);
        this.power = new MageInt(2);
        this.toughness = new MageInt(4);

        // {T}, Untap a tapped land an opponent controls: Add one mana of any type that land could produce.
        this.addAbility(new ComposedManaAbilityBuilder()
                .cost(new TapSourceCost())
                .cost(new UntapTargetCost(new TargetPermanent(filter)))
                .addChoice(BenthicExplorersManaType.instance, 1)
                .ruleText("Add one mana of any type that land could produce")
                .build()
        );
    }

    private BenthicExplorers(final BenthicExplorers card) {
        super(card);
    }

    @Override
    public BenthicExplorers copy() {
        return new BenthicExplorers(this);
    }
}

enum BenthicExplorersManaType implements ManaTypeProvider {
    instance;

    @Override
    public Set<ManaType> getManaTypes(Game game, Ability source, Effect effect) {
        if (game == null) {
            return Collections.emptySet();
        }
        Set<ManaType> manaTypes = getUntappedManaTypes(game, source);
        if (manaTypes.isEmpty()) {
            for (UUID opponentId : game.getOpponents(source.getControllerId())) {
                for (Permanent permanent : game.getBattlefield().getAllActivePermanents(opponentId)) {
                    if (permanent.isLand(game) && permanent.isTapped()) {
                        for (ActivatedManaAbilityImpl ability : permanent.getAbilities(game).getActivatedManaAbilities(Zone.BATTLEFIELD)) {
                            manaTypes.addAll(ability.getProducableManaTypes(game));
                        }
                    }
                }
            }
        }
        return manaTypes;
    }

    private Set<ManaType> getUntappedManaTypes(Game game, Ability source) {
        Set<ManaType> types = EnumSet.noneOf(ManaType.class);
        if (game == null || game.getPhase() == null) {
            return types;
        }

        List<UUID> untapped = (List<UUID>) game.getState()
                .getValue("UntapTargetCost" + source.getSourceId().toString());
        if (untapped == null || untapped.isEmpty()) {
            return types;
        }
        Permanent land = game.getPermanentOrLKIBattlefield(untapped.getFirst());
        if (land == null) {
            return types;
        }

        Abilities<ActivatedManaAbilityImpl> mana = land.getAbilities().getActivatedManaAbilities(Zone.BATTLEFIELD);
        for (ActivatedManaAbilityImpl ability : mana) {
            if (ability.definesMana(game)) {
                types.addAll(ability.getProducableManaTypes(game));
            }
        }
        return types;
    }
}
