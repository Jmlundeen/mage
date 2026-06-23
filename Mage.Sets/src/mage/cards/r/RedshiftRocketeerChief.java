package mage.cards.r;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.dynamicvalue.common.SourcePermanentPowerValue;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.keyword.ExhaustAbility;
import mage.abilities.keyword.VigilanceAbility;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.abilities.mana.conditional.ActivatedAbilityManaCondition;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.cards.Cards;
import mage.cards.CardsImpl;
import mage.constants.*;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.players.Player;
import mage.target.TargetCard;
import mage.target.common.TargetCardInHand;

import java.util.UUID;

/**
 * @author TheElk801
 */
public final class RedshiftRocketeerChief extends CardImpl {

    public RedshiftRocketeerChief(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{R}{G}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.GOBLIN);
        this.subtype.add(SubType.PILOT);
        this.power = new MageInt(2);
        this.toughness = new MageInt(3);

        // Vigilance
        this.addAbility(VigilanceAbility.getInstance());

        // {T}: Add X mana of any one color, where X is Redshift's power. Spend this mana only to activate abilities.
        this.addAbility(new ComposedManaAbilityBuilder()
                .cost(new TapSourceCost())
                .addChoiceAnyOneColor(SourcePermanentPowerValue.NOT_NEGATIVE)
                .condition(new ActivatedAbilityManaCondition())
                .ruleText("{T}: Add X mana of any one color, where X is {this}'s power. Spend this mana only to activate abilities.")
                .build()
        );

        // Exhaust -- {10}{R}{G}: Put any number of permanent cards from your hand onto the battlefield.
        this.addAbility(new ExhaustAbility(new RedshiftRocketeerChiefEffect(), new ManaCostsImpl<>("{10}{R}{G}")));
    }

    private RedshiftRocketeerChief(final RedshiftRocketeerChief card) {
        super(card);
    }

    @Override
    public RedshiftRocketeerChief copy() {
        return new RedshiftRocketeerChief(this);
    }
}

class RedshiftRocketeerChiefEffect extends OneShotEffect {

    RedshiftRocketeerChiefEffect() {
        super(Outcome.Benefit);
        staticText = "put any number of permanent cards from your hand onto the battlefield";
    }

    private RedshiftRocketeerChiefEffect(final RedshiftRocketeerChiefEffect effect) {
        super(effect);
    }

    @Override
    public RedshiftRocketeerChiefEffect copy() {
        return new RedshiftRocketeerChiefEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player player = game.getPlayer(source.getControllerId());
        if (player == null) {
            return false;
        }
        TargetCard target = new TargetCardInHand(0, Integer.MAX_VALUE, StaticFilters.FILTER_CARD_PERMANENTS);
        player.choose(outcome, player.getHand(), target, source, game);
        Cards cards = new CardsImpl(target.getTargets());
        return !cards.isEmpty() && player.moveCards(cards, Zone.BATTLEFIELD, source, game);
    }
}
