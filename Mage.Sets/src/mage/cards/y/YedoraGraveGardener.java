package mage.cards.y;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.DiesCreatureTriggeredAbility;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.mana.GreenManaAbility;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.cards.CopiableValues;
import mage.constants.*;
import mage.filter.FilterPermanent;
import mage.filter.common.FilterControlledCreaturePermanent;
import mage.filter.predicate.mageobject.AnotherPredicate;
import mage.filter.predicate.permanent.TokenPredicate;
import mage.game.Game;
import mage.game.MoveCardsParameters;
import mage.players.Player;

import java.util.UUID;

/**
 * @author TheElk801
 */
public final class YedoraGraveGardener extends CardImpl {

    private static final FilterPermanent filter = new FilterControlledCreaturePermanent("another nontoken creature you control");

    static {
        filter.add(TokenPredicate.FALSE);
        filter.add(AnotherPredicate.instance);
    }

    public YedoraGraveGardener(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{4}{G}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.TREEFOLK);
        this.subtype.add(SubType.DRUID);
        this.power = new MageInt(5);
        this.toughness = new MageInt(5);

        // Whenever another nontoken creature you control dies, you may return it to the battlefield face down under its owner's control. It's a Forest land.
        this.addAbility(new DiesCreatureTriggeredAbility(
                new YedoraGraveGardenerEffect(), true, filter, true
        ));
    }

    private YedoraGraveGardener(final YedoraGraveGardener card) {
        super(card);
    }

    @Override
    public YedoraGraveGardener copy() {
        return new YedoraGraveGardener(this);
    }
}

class YedoraGraveGardenerEffect extends OneShotEffect {

    YedoraGraveGardenerEffect() {
        super(Outcome.Benefit);
        staticText = "you may return it to the battlefield face down under its owner's control. " +
                "It's a Forest land. <i>(It has no other types or abilities.)</i>";
    }

    private YedoraGraveGardenerEffect(final YedoraGraveGardenerEffect effect) {
        super(effect);
    }

    @Override
    public YedoraGraveGardenerEffect copy() {
        return new YedoraGraveGardenerEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player player = game.getPlayer(source.getControllerId());
        Card card = game.getCard(getTargetPointer().getFirst(game, source));
        if (player == null || card == null) {
            return false;
        }
        CopiableValues faceDownValues = new CopiableValues(true);
        faceDownValues.getCardType().add(CardType.LAND);
        faceDownValues.getSubtype().add(SubType.FOREST);
        faceDownValues.setPower(new MageInt(0));
        faceDownValues.setToughness(new MageInt(0));
        faceDownValues.getAbilities().add(new GreenManaAbility());
        MoveCardsParameters parameters = new MoveCardsParameters(card, Zone.BATTLEFIELD)
                .setFaceDownValues(faceDownValues)
                .setFaceDown(true)
                .setByOwner(true);
        player.moveCards(parameters, source, game);
        return true;
    }
}
