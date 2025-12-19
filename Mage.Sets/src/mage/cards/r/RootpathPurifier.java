package mage.cards.r;

import mage.MageInt;
import mage.MageItem;
import mage.MageObject;
import mage.abilities.Ability;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.players.Player;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author TheElk801
 */
public final class RootpathPurifier extends CardImpl {

    public RootpathPurifier(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{3}{G}");

        this.subtype.add(SubType.ELF);
        this.subtype.add(SubType.DRUID);
        this.power = new MageInt(3);
        this.toughness = new MageInt(4);

        // Lands you control and land cards in your library are basic.
        this.addAbility(new SimpleStaticAbility(new RootpathPurifierEffect()));
    }

    private RootpathPurifier(final RootpathPurifier card) {
        super(card);
    }

    @Override
    public RootpathPurifier copy() {
        return new RootpathPurifier(this);
    }
}

class RootpathPurifierEffect extends ContinuousEffectImpl {

    RootpathPurifierEffect() {
        super(Duration.WhileOnBattlefield, Layer.TypeChangingEffects_4, SubLayer.NA, Outcome.Benefit);
        staticText = "lands you control and land cards in your library are basic";
        dependendToTypes.add(DependencyType.BecomeNonbasicLand);
    }

    private RootpathPurifierEffect(final RootpathPurifierEffect effect) {
        super(effect);
    }

    @Override
    public RootpathPurifierEffect copy() {
        return new RootpathPurifierEffect(this);
    }

    @Override
    public void applyToObjects(Layer layer, SubLayer sublayer, Ability source, Game game, List<MageItem> affectedObjects) {
        for (MageItem object : affectedObjects) {
            ((MageObject) object).addSuperType(game, SuperType.BASIC);
        }
    }

    @Override
    public boolean queryAffectedObjects(Layer layer, Ability source, Game game, List<MageItem> affectedObjects) {
        Player player = game.getPlayer(source.getControllerId());
        if (player == null) {
            return false;
        }
        affectedObjects.addAll(game.getBattlefield().getActivePermanents(
                StaticFilters.FILTER_CONTROLLED_PERMANENT_LAND,
                source.getControllerId(), source, game
        ));
        affectedObjects.addAll(player.getLibrary().getCards(game).stream()
                .filter(Card::isLand)
                .collect(Collectors.toList()));
        return !affectedObjects.isEmpty();
    }
}
