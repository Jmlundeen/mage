package mage.cards.y;

import mage.MageInt;
import mage.MageItem;
import mage.MageObjectReference;
import mage.abilities.Ability;
import mage.abilities.common.DiesCreatureTriggeredAbility;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.mana.GreenManaAbility;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.FilterPermanent;
import mage.filter.common.FilterControlledCreaturePermanent;
import mage.filter.predicate.mageobject.AnotherPredicate;
import mage.filter.predicate.permanent.TokenPredicate;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.target.targetpointer.FixedTarget;

import java.util.List;
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
        game.addEffect(new YedoraGraveGardenerContinuousEffect().setTargetPointer(
                new FixedTarget(new MageObjectReference(card, game, 1))
        ), source);
        player.moveCards(
                card, Zone.BATTLEFIELD, source, game,
                false, true, true, null
        );
        return true;
    }
}

class YedoraGraveGardenerContinuousEffect extends ContinuousEffectImpl {

    YedoraGraveGardenerContinuousEffect() {
        super(Duration.Custom, Layer.CopyEffects_1, SubLayer.FaceDownEffects_1b, Outcome.Neutral);
    }

    private YedoraGraveGardenerContinuousEffect(final YedoraGraveGardenerContinuousEffect effect) {
        super(effect);
    }

    @Override
    public YedoraGraveGardenerContinuousEffect copy() {
        return new YedoraGraveGardenerContinuousEffect(this);
    }

    @Override
    public void applyToObjects(Layer layer, SubLayer sublayer, Ability source, Game game, List<MageItem> affectedObjects) {
        for (MageItem object : affectedObjects) {
            Permanent permanent = (Permanent) object;
            permanent.removeAllSuperTypes(game);
            permanent.removeAllCardTypes(game);
            permanent.removeAllSubTypes(game);
            permanent.addCardType(game, CardType.LAND);
            permanent.addSubType(game, SubType.FOREST);
            permanent.removeAllAbilities(source.getSourceId(), game);
            permanent.addAbility(new GreenManaAbility(), source.getSourceId(), game);
        }
    }

    @Override
    public boolean queryAffectedObjects(Layer layer, Ability source, Game game, List<MageItem> affectedObjects) {
        Permanent permanent = game.getPermanent(getTargetPointer().getFirst(game, source));
        if (permanent == null || !permanent.isFaceDown(game)) {
            discard();
            return false;
        }
        affectedObjects.add(permanent);
        return true;
    }
}
