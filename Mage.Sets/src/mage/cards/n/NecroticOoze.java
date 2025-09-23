package mage.cards.n;

import mage.MageInt;
import mage.MageItem;
import mage.abilities.Ability;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.players.Player;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author BetaSteward_at_googlemail.com
 */
public final class NecroticOoze extends CardImpl {

    public NecroticOoze(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{2}{B}{B}");
        this.subtype.add(SubType.OOZE);

        this.power = new MageInt(4);
        this.toughness = new MageInt(3);

        // As long as Necrotic Ooze is on the battlefield, it has all 
        // activated abilities of all creature cards in all graveyards
        this.addAbility(new SimpleStaticAbility(new NecroticOozeEffect()));
    }

    private NecroticOoze(final NecroticOoze card) {
        super(card);
    }

    @Override
    public NecroticOoze copy() {
        return new NecroticOoze(this);
    }
}

class NecroticOozeEffect extends ContinuousEffectImpl {

    NecroticOozeEffect() {
        super(Duration.WhileOnBattlefield, Layer.AbilityAddingRemovingEffects_6, SubLayer.NA, Outcome.AddAbility);
        staticText = "As long as {this} is on the battlefield, "
                + "it has all activated abilities of all creature cards in all graveyards";
        this.dependendToTypes.add(DependencyType.AddingAbility); // Yixlid Jailer
    }

    private NecroticOozeEffect(final NecroticOozeEffect effect) {
        super(effect);
    }

    @Override
    public void applyToObjects(Layer layer, SubLayer sublayer, Ability source, Game game, List<MageItem> affectedObjects) {
        Set<Ability> abilities = game
                .getState()
                .getPlayersInRange(source.getControllerId(), game)
                .stream()
                .map(game::getPlayer)
                .filter(Objects::nonNull)
                .map(Player::getGraveyard)
                .map(graveyard -> graveyard.getCards(StaticFilters.FILTER_CARD_CREATURE, game))
                .flatMap(Collection::stream)
                .map(card -> card.getAbilities(game))
                .flatMap(Collection::stream)
                .filter(Ability::isActivatedAbility)
                .collect(Collectors.toSet());
        for (MageItem object : affectedObjects) {
            for (Ability ability : abilities) {
                ((Permanent) object).addAbility(ability, source.getSourceId(), game, true);
            }
        }
    }

    @Override
    public boolean queryAffectedObjects(Layer layer, Ability source, Game game, List<MageItem> affectedObjects) {
        Permanent permanent = source.getSourcePermanentIfItStillExists(game);
        if (permanent != null) {
            affectedObjects.add(permanent);
            return true;
        }
        return false;
    }

    @Override
    public NecroticOozeEffect copy() {
        return new NecroticOozeEffect(this);
    }
}
