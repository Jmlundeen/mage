package mage.cards.r;

import mage.MageItem;
import mage.abilities.Ability;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.condition.common.MyTurnCondition;
import mage.abilities.decorator.ConditionalContinuousEffect;
import mage.abilities.effects.ContinuousEffect;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.effects.common.continuous.generic.ContinuousEffectBuilder;
import mage.abilities.hint.common.MyTurnHint;
import mage.abilities.keyword.FlashbackAbility;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.players.Player;

import java.util.List;
import java.util.UUID;

/**
 *
 * @author notgreat
 */
public final class ReturnThePast extends CardImpl {

    public ReturnThePast(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ENCHANTMENT}, "{4}{R}{R}");


        // As long as it's your turn, each instant and sorcery card in your graveyard has flashback. Its flashback cost is equal to its mana cost.
        ContinuousEffect effect = new ContinuousEffectBuilder(Duration.WhileOnBattlefield, Outcome.AddAbility)
                .setAffectedZones(Zone.GRAVEYARD)
                .setCardFilter(StaticFilters.FILTER_CARD_INSTANT_OR_SORCERY)
                .withGainedAbility((card, source, game) -> new FlashbackAbility(card, card.getManaCost()));
        this.addAbility(new SimpleStaticAbility(new ConditionalContinuousEffect(
                effect,
                MyTurnCondition.instance,
                "During your turn, each instant and sorcery card in your graveyard has flashback. " +
                        "Its flashback cost is equal to its mana cost"
        )).addHint(MyTurnHint.instance));
    }

    private ReturnThePast(final ReturnThePast card) {
        super(card);
    }

    @Override
    public ReturnThePast copy() {
        return new ReturnThePast(this);
    }
}
