package mage.cards.n;

import mage.abilities.Ability;
import mage.abilities.LoyaltyAbility;
import mage.abilities.common.delayed.ReflexiveTriggeredAbility;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.DamageTargetEffect;
import mage.abilities.effects.common.GainLifeEffect;
import mage.abilities.effects.common.GetEmblemEffect;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.abilities.mana.conditional.FilteredSpellManaCondition;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.StaticTypedFilters;
import mage.game.Game;
import mage.game.command.emblems.NarsetOfTheAncientWayEmblem;
import mage.players.Player;
import mage.target.common.TargetCreatureOrPlaneswalker;

import java.util.Set;
import java.util.UUID;

/**
 * @author TheElk801
 */
public final class NarsetOfTheAncientWay extends CardImpl {

    public NarsetOfTheAncientWay(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.PLANESWALKER}, "{1}{U}{R}{W}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.NARSET);
        this.setStartingLoyalty(4);

        // +1: You gain 2 life. Add {U}, {R}, or {W}. Spend this mana only to cast a noncreature spell.
        Ability ability = new LoyaltyAbility(new GainLifeEffect(2), 1);
        ability.addEffect(new ComposedManaAbilityBuilder()
                .addChoice(Set.of(ManaType.BLUE, ManaType.RED, ManaType.WHITE), 1)
                .condition(new FilteredSpellManaCondition(StaticTypedFilters.A_NON_CREATURE_SPELL))
                .ruleText("Add {U}, {R}, or {W}. Spend this mana only to cast a noncreature spell")
                .buildEffect());
        this.addAbility(ability);

        // −2: Draw a card, then you may discard a card. When you discard a nonland card this way, Narset of the Ancient Way deals damage equal to that card's converted mana cost to target creature or planeswalker.
        this.addAbility(new LoyaltyAbility(new NarsetOfTheAncientWayDrawEffect(), -2));

        // −6: You get an emblem with "Whenever you cast a noncreature spell, this emblem deals 2 damage to any target."
        this.addAbility(new LoyaltyAbility(new GetEmblemEffect(new NarsetOfTheAncientWayEmblem()), -6));
    }

    private NarsetOfTheAncientWay(final NarsetOfTheAncientWay card) {
        super(card);
    }

    @Override
    public NarsetOfTheAncientWay copy() {
        return new NarsetOfTheAncientWay(this);
    }
}

class NarsetOfTheAncientWayDrawEffect extends OneShotEffect {

    NarsetOfTheAncientWayDrawEffect() {
        super(Outcome.Benefit);
        staticText = "Draw a card, then you may discard a card. When you discard a nonland card this way, " +
                "{this} deals damage equal to that card's mana value to target creature or planeswalker.";
    }

    private NarsetOfTheAncientWayDrawEffect(final NarsetOfTheAncientWayDrawEffect effect) {
        super(effect);
    }

    @Override
    public NarsetOfTheAncientWayDrawEffect copy() {
        return new NarsetOfTheAncientWayDrawEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player player = game.getPlayer(source.getControllerId());
        if (player == null) {
            return false;
        }
        player.drawCards(1, source, game);
        if (player.getHand().isEmpty() || !player.chooseUse(Outcome.Discard, "Discard a card?", source, game)) {
            return false;
        }
        Card card = player.discardOne(false, false, source, game);
        if (card == null || card.isLand(game)) {
            return false;
        }
        ReflexiveTriggeredAbility ability = new ReflexiveTriggeredAbility(
                new DamageTargetEffect(card.getManaValue()), false, "{this} deals damage " +
                "to target creature or planeswalker equal to the discarded card's mana value"
        );
        ability.addTarget(new TargetCreatureOrPlaneswalker());
        game.fireReflexiveTriggeredAbility(ability, source);
        return true;
    }
}
