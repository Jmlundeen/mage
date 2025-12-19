package mage.cards.m;

import mage.MageItem;
import mage.abilities.Ability;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.costs.mana.GenericManaCost;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.effects.common.CreateTokenEffect;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.common.FilterControlledCreaturePermanent;
import mage.game.Game;
import mage.game.command.CommandObject;
import mage.game.command.Commander;
import mage.game.permanent.Permanent;
import mage.game.permanent.token.ShapeshifterBlueToken;
import mage.game.stack.Spell;
import mage.game.stack.StackObject;
import mage.players.Player;
import mage.util.CardUtil;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * @author TheElk801
 */
public final class MaskwoodNexus extends CardImpl {

    public MaskwoodNexus(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT}, "{4}");

        // Creatures you control are every creature type. The same is true for creature spells you control and creature cards you own that aren't on the battlefield.
        this.addAbility(new SimpleStaticAbility(new MaskwoodNexusEffect()));

        // {3}, {T}: Create a 2/2 blue Shapeshifter creature token with changeling.
        Ability ability = new SimpleActivatedAbility(
                new CreateTokenEffect(new ShapeshifterBlueToken()), new GenericManaCost(3)
        );
        ability.addCost(new TapSourceCost());
        this.addAbility(ability);
    }

    private MaskwoodNexus(final MaskwoodNexus card) {
        super(card);
    }

    @Override
    public MaskwoodNexus copy() {
        return new MaskwoodNexus(this);
    }
}

class MaskwoodNexusEffect extends ContinuousEffectImpl {

    MaskwoodNexusEffect() {
        super(Duration.WhileOnBattlefield, Layer.TypeChangingEffects_4, SubLayer.NA, Outcome.Benefit);
        staticText = "Creatures you control are every creature type. "
                + "The same is true for creature spells you control "
                + "and creature cards you own that aren't on the battlefield.";
        this.dependendToTypes.add(DependencyType.BecomeCreature);
    }

    private MaskwoodNexusEffect(final MaskwoodNexusEffect effect) {
        super(effect);
    }

    @Override
    public MaskwoodNexusEffect copy() {
        return new MaskwoodNexusEffect(this);
    }

    @Override
    public void applyToObjects(Layer layer, SubLayer sublayer, Ability source, Game game, List<MageItem> affectedObjects) {
        for (MageItem object : affectedObjects) {
            if (object instanceof Permanent) {
                ((Permanent) object).setIsAllCreatureTypes(game, true);
            }
            if (object instanceof Card) {
                // apply to all parts
                Card card = (Card) object;
                game.getState().getCreateMageObjectAttribute(card, game).getSubtype().setIsAllCreatureTypes(true);
                CardUtil.getObjectParts(card).stream()
                        .map(game::getObject)
                        .filter(Objects::nonNull)
                        .filter(part -> part.isCreature(game))
                        .forEach(mageObject -> game.getState().getCreateMageObjectAttribute(mageObject, game)
                                .getSubtype().setIsAllCreatureTypes(true)
                        );
            }
        }
    }

    @Override
    public boolean queryAffectedObjects(Layer layer, Ability source, Game game, List<MageItem> affectedObjects) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        // Creature cards you own that aren't on the battlefield
        // in graveyard
        for (UUID cardId : controller.getGraveyard()) {
            Card card = game.getCard(cardId);
            if (card != null && card.isCreature(game) && !card.isArtifact(game)) {
                affectedObjects.add(card);
            }
        }
        // on Hand
        for (UUID cardId : controller.getHand()) {
            Card card = game.getCard(cardId);
            if (card != null && card.isCreature(game) && !card.isArtifact(game)) {
                affectedObjects.add(card);
            }
        }
        // in Exile
        for (Card card : game.getState().getExile().getCardsOwned(game, source.getControllerId())) {
            if (card.isCreature(game) && !card.isArtifact(game)) {
                affectedObjects.add(card);
            }
        }
        // in Library (e.g. for Mystical Teachings)
        for (Card card : controller.getLibrary().getCards(game)) {
            if (card.isOwnedBy(controller.getId()) && card.isCreature(game) && !card.isArtifact(game)) {
                affectedObjects.add(card);
            }
        }
        // commander in command zone
        for (CommandObject commandObject : game.getState().getCommand()) {
            if (commandObject instanceof Commander) {
                Card card = game.getCard((commandObject).getId());
                if (card != null && card.isOwnedBy(controller.getId())
                        && card.isCreature(game) && !card.isArtifact(game)) {
                    affectedObjects.add(card);
                }
            }
        }

        // creature spells you control
        for (StackObject stackObject : game.getStack()) {
            if (stackObject instanceof Spell
                    && stackObject.isControlledBy(source.getControllerId())
                    && stackObject.isCreature(game)
                    && !stackObject.isArtifact(game)) {
                Card card = ((Spell) stackObject).getCard();
                affectedObjects.add(card);
            }
        }
        // creatures you control
        List<Permanent> creatures = game.getBattlefield().getAllActivePermanents(
                new FilterControlledCreaturePermanent(), source.getControllerId(), game);
        for (Permanent creature : creatures) {
            if (creature != null) {
                affectedObjects.add(creature);
            }
        }
        return !affectedObjects.isEmpty();
    }
}
