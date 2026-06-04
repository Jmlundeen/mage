package mage.cards.i;

import mage.MageInt;
import mage.MageObject;
import mage.abilities.Ability;
import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.dynamicvalue.DynamicValue;
import mage.abilities.effects.Effect;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.ExileUntilSourceLeavesEffect;
import mage.abilities.effects.common.continuous.layers.L6_Abilities.GainAbilitiesOfEffect;
import mage.abilities.keyword.VanishingAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.FilterTyped;
import mage.filter.StaticFilters;
import mage.filter.predicate.typed.LogicalPredicate;
import mage.filter.predicate.typed.ability.ActivatedAbilityPredicate;
import mage.filter.predicate.typed.ability.TriggeredAbilityPredicate;
import mage.filter.predicate.typed.card.CardPredicate;
import mage.game.ExileZone;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.target.TargetPermanent;
import mage.target.targetpointer.FixedTarget;
import mage.util.CardUtil;

import java.util.UUID;

/**
 * @author TheElk801
 */
public final class IdrisSoulOfTheTARDIS extends CardImpl {

    static final FilterTyped filter = new FilterTyped("activated or triggered abilities")
            .add(CardPredicate.instance)
            .add(
                    LogicalPredicate.or(
                            ActivatedAbilityPredicate.instance,
                            TriggeredAbilityPredicate.instance
                    )
            );

    public IdrisSoulOfTheTARDIS(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{1}{U}{R}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.INCARNATION);
        this.power = new MageInt(3);
        this.toughness = new MageInt(3);

        // Vanishing 3
        this.addAbility(new VanishingAbility(3));

        // Imprint -- When Idris, Soul of the TARDIS enters the battlefield, exile another artifact you control until Idris leaves the battlefield.
        this.addAbility(new EntersBattlefieldTriggeredAbility(new IdrisSoulOfTheTARDISExileEffect()).setAbilityWord(AbilityWord.IMPRINT));

        // Idris has all activated and triggered abilities of the exiled card and gets +X/+X, where X is the exiled card's mana value.
        this.addAbility(new SimpleStaticAbility(new GainAbilitiesOfEffect()
                .setAbilityFilter(filter)
                .fromSourceExiled()
                .withAddPower(ExiledCardManaValue.instance)
                .withAddToughness(ExiledCardManaValue.instance)
                .setText("Idris has all activated and triggered abilities of the exiled card and gets +X/+X, where X is the exiled card's mana value")
        ));
    }

    private IdrisSoulOfTheTARDIS(final IdrisSoulOfTheTARDIS card) {
        super(card);
    }

    @Override
    public IdrisSoulOfTheTARDIS copy() {
        return new IdrisSoulOfTheTARDIS(this);
    }
}

class IdrisSoulOfTheTARDISExileEffect extends OneShotEffect {

    IdrisSoulOfTheTARDISExileEffect() {
        super(Outcome.Benefit);
        staticText = "exile another artifact you control until {this} leaves the battlefield";
    }

    private IdrisSoulOfTheTARDISExileEffect(final IdrisSoulOfTheTARDISExileEffect effect) {
        super(effect);
    }

    @Override
    public IdrisSoulOfTheTARDISExileEffect copy() {
        return new IdrisSoulOfTheTARDISExileEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player player = game.getPlayer(source.getControllerId());
        if (player == null || !game.getBattlefield().contains(
                StaticFilters.FILTER_CONTROLLED_ANOTHER_ARTIFACT, source, game, 1
        )) {
            return false;
        }
        TargetPermanent target = new TargetPermanent(StaticFilters.FILTER_CONTROLLED_ANOTHER_ARTIFACT);
        target.withNotTarget(true);
        player.choose(outcome, target, source, game);
        Permanent permanent = game.getPermanent(target.getFirstTarget());
        return permanent != null
                && new ExileUntilSourceLeavesEffect()
                .setTargetPointer(new FixedTarget(permanent, game))
                .apply(game, source);
    }
}

enum ExiledCardManaValue implements DynamicValue {
    instance;

    @Override
    public int calculate(Game game, Ability source, Effect effect) {
        ExileZone exileZone = game.getExile().getExileZone(CardUtil.getExileZoneId(
                game, source.getSourceId(), game.getState().getZoneChangeCounter(source.getSourceId())
        ));
        if (exileZone == null || exileZone.isEmpty()) {
            return 0;
        }
        return exileZone
                .getCards(game)
                .stream()
                .mapToInt(MageObject::getManaValue)
                .sum();
    }

    @Override
    public ExiledCardManaValue copy() {
        return instance;
    }

    @Override
    public String getMessage() {
        return "the exiled card's mana value";
    }
}