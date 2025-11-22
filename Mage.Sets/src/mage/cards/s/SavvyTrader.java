package mage.cards.s;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.asthought.PlayFromNotOwnHandZoneTargetEffect;
import mage.abilities.effects.common.cost.SpellsCostReductionControllerEffect;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.FilterCard;
import mage.filter.StaticFilters;
import mage.filter.predicate.other.SpellCastFromAnywhereOtherThanHand;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.target.common.TargetCardInYourGraveyard;

import java.util.UUID;

/**
 * @author Susucr
 */
public final class SavvyTrader extends CardImpl {

    private static final FilterCard filter = new FilterCard("Spells you cast from anywhere other than your hand");

    static {
        filter.add(SpellCastFromAnywhereOtherThanHand.instance);
    }

    public SavvyTrader(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{3}{G}");

        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.CITIZEN);
        this.power = new MageInt(3);
        this.toughness = new MageInt(3);

        // When Savvy Trader enters the battlefield, exile target permanent card from your graveyard. You may play that card for as long as it remains exiled.
        Ability ability = new EntersBattlefieldTriggeredAbility(new SavvyTraderEffect());
        ability.addTarget(new TargetCardInYourGraveyard(StaticFilters.FILTER_CARD_PERMANENT));
        this.addAbility(ability);

        // Spells you cast from anywhere other than your hand cost {1} less to cast.
        this.addAbility(new SimpleStaticAbility(new SpellsCostReductionControllerEffect(filter, 1)));
    }

    private SavvyTrader(final SavvyTrader card) {
        super(card);
    }

    @Override
    public SavvyTrader copy() {
        return new SavvyTrader(this);
    }
}

class SavvyTraderEffect extends OneShotEffect {

    SavvyTraderEffect() {
        super(Outcome.DrawCard);
        staticText = "exile target permanent card from your graveyard. "
                + "You may play that card for as long as it remains exiled";
    }

    private SavvyTraderEffect(final SavvyTraderEffect effect) {
        super(effect);
    }

    @Override
    public SavvyTraderEffect copy() {
        return new SavvyTraderEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        Permanent sourcePermanent = game.getPermanentOrLKIBattlefield(source.getSourceId());
        Card card = game.getCard(source.getFirstTarget());
        if (sourcePermanent == null || controller == null || card == null) {
            return false;
        }
        PlayFromNotOwnHandZoneTargetEffect.exileAndPlayFromExile(game, source, card, TargetController.YOU, Duration.EndOfGame, false, false, false);
        return true;
    }
}