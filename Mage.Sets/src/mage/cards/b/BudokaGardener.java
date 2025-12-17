package mage.cards.b;

import mage.abilities.Ability;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.dynamicvalue.common.PermanentsOnBattlefieldCount;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.CreateTokenEffect;
import mage.abilities.effects.common.FlipSourceEffect;
import mage.abilities.effects.common.PutCardFromHandOntoBattlefieldEffect;
import mage.abilities.hint.ValueHint;
import mage.cards.CardSetInfo;
import mage.cards.FlipCard;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.filter.StaticFilters;
import mage.filter.common.FilterControlledLandPermanent;
import mage.filter.common.FilterControlledPermanent;
import mage.game.Game;
import mage.game.permanent.token.DokaiWeaverofLifeToken;
import mage.players.Player;

import java.util.UUID;

/**
 * @author Loki
 */
public final class BudokaGardener extends FlipCard {

    public BudokaGardener(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo,
                new CardType[]{CardType.CREATURE}, new SubType[]{SubType.HUMAN, SubType.MONK}, "{1}{G}",
                "Dokai, Weaver of Life",
                new SuperType[]{SuperType.LEGENDARY}, new CardType[]{CardType.CREATURE}, new SubType[]{SubType.HUMAN, SubType.MONK});

        // Budoka Gardener
        this.getLeftHalfCard().setPT(2, 1);

        // {T}: You may put a land card from your hand onto the battlefield. If you control ten or more lands, flip Budoka Gardener.
        Ability ability = new SimpleActivatedAbility(new PutCardFromHandOntoBattlefieldEffect(StaticFilters.FILTER_CARD_LAND_A), new TapSourceCost());
        ability.addEffect(new BudokaGardenerEffect());
        this.getLeftHalfCard().addAbility(ability.addHint(new ValueHint("Lands you control", new PermanentsOnBattlefieldCount(StaticFilters.FILTER_CONTROLLED_PERMANENT_LAND))));

        // Dokai, Weaver of Life
        this.getRightHalfCard().setPT(3, 3);

        // {4}{G}{G}, {T}: Create an X/X green Elemental creature token, where X is the number of lands you control.
        Ability ability2 = new SimpleActivatedAbility(new CreateTokenEffect(new DokaiWeaverofLifeToken()), new ManaCostsImpl<>("{4}{G}{G}"));
        ability2.addCost(new TapSourceCost());
        this.getRightHalfCard().addAbility(ability2);
    }

    private BudokaGardener(final BudokaGardener card) {
        super(card);
    }

    @Override
    public BudokaGardener copy() {
        return new BudokaGardener(this);
    }

}

class BudokaGardenerEffect extends OneShotEffect {

    static final FilterControlledPermanent filterLands = new FilterControlledLandPermanent("lands you control");

    BudokaGardenerEffect() {
        super(Outcome.PutLandInPlay);
        staticText = "If you control ten or more lands, flip {this}";
    }

    private BudokaGardenerEffect(final BudokaGardenerEffect effect) {
        super(effect);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        if (game.getBattlefield().count(filterLands, source.getControllerId(), source, game) < 10) {
            return false;
        }

        return new FlipSourceEffect().apply(game, source);
    }

    @Override
    public BudokaGardenerEffect copy() {
        return new BudokaGardenerEffect(this);
    }

}
