package mage.abilities.mana.providers.common.manaType;

import mage.abilities.Ability;
import mage.abilities.effects.Effect;
import mage.abilities.mana.providers.ManaTypeProvider;
import mage.cards.Card;
import mage.constants.CommanderCardType;
import mage.constants.ManaType;
import mage.filter.FilterMana;
import mage.game.Game;
import mage.players.Player;

import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

/**
 * Mana types in your commander's color identity.
 * @author Jmlundeen
 */
public enum CommanderColorIdentityManaTypes implements ManaTypeProvider {
    instance;

    @Override
    public Set<ManaType> getManaTypes(Game game, Ability source, Effect effect) {
        Set<ManaType> manaTypes = EnumSet.noneOf(ManaType.class);
        if (game == null || source == null) {
            return manaTypes;
        }

        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return manaTypes;
        }

        for (UUID commanderId : game.getCommandersIds(controller, CommanderCardType.COMMANDER_OR_OATHBREAKER, false)) {
            Card commander = game.getCard(commanderId);
            if (commander == null) {
                continue;
            }

            FilterMana colorIdentity = commander.getColorIdentity();
            if (colorIdentity.isWhite()) {
                manaTypes.add(ManaType.WHITE);
            }
            if (colorIdentity.isBlue()) {
                manaTypes.add(ManaType.BLUE);
            }
            if (colorIdentity.isBlack()) {
                manaTypes.add(ManaType.BLACK);
            }
            if (colorIdentity.isRed()) {
                manaTypes.add(ManaType.RED);
            }
            if (colorIdentity.isGreen()) {
                manaTypes.add(ManaType.GREEN);
            }
        }

        return manaTypes;
    }
}

