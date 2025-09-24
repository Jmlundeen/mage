package mage.cards.w;

import mage.MageInt;
import mage.MageItem;
import mage.abilities.Ability;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.keyword.EncoreAbility;
import mage.abilities.keyword.FearAbility;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.common.FilterCreatureCard;
import mage.game.Game;
import mage.players.Player;

import java.util.List;
import java.util.UUID;

/**
 * @author PurpleCrowbar
 */
public final class WireSurgeons extends CardImpl {

    public WireSurgeons(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{4}{B}{B}");
        this.subtype.add(SubType.HUMAN, SubType.ARTIFICER);
        this.power = new MageInt(6);
        this.toughness = new MageInt(5);

        // Fear
        this.addAbility(FearAbility.getInstance());

        // Each artifact creature card in your graveyard has encore. Its encore cost is equal to its mana cost.
        this.addAbility(new SimpleStaticAbility(new WireSurgeonsEffect()));
    }

    private WireSurgeons(final WireSurgeons card) {
        super(card);
    }

    @Override
    public WireSurgeons copy() {
        return new WireSurgeons(this);
    }
}

class WireSurgeonsEffect extends ContinuousEffectImpl {

    private static final FilterCreatureCard filter = new FilterCreatureCard("artifact creature card");

    static {
        filter.add(CardType.ARTIFACT.getPredicate());
    }

    public WireSurgeonsEffect() {
        super(Duration.WhileOnBattlefield, Layer.AbilityAddingRemovingEffects_6, SubLayer.NA, Outcome.AddAbility);
        this.staticText = "Each artifact creature card in your graveyard has encore. " +
                "Its encore cost is equal to its mana cost.";
    }

    private WireSurgeonsEffect(final WireSurgeonsEffect effect) {
        super(effect);
    }

    @Override
    public WireSurgeonsEffect copy() {
        return new WireSurgeonsEffect(this);
    }

    @Override
    public void applyToObjects(Layer layer, SubLayer sublayer, Ability source, Game game, List<MageItem> affectedObjects) {
        for (MageItem object : affectedObjects) {
            Card card = (Card) object;
            Ability ability = new EncoreAbility(card.getManaCost());
            game.getState().addOtherAbility(card, ability);
        }
    }

    @Override
    public boolean queryAffectedObjects(Layer layer, Ability source, Game game, List<MageItem> affectedObjects) {
        Player player = game.getPlayer(source.getControllerId());
        if (player == null) {
            return false;
        }
        affectedObjects.addAll(player.getGraveyard().getCards(filter, game));
        return !affectedObjects.isEmpty();
    }
}
