package mage.abilities.mana.conditional;

import mage.MageObject;
import mage.abilities.Ability;
import mage.abilities.SpellAbility;
import mage.abilities.costs.Cost;
import mage.cards.Card;
import mage.filter.FilterTyped;
import mage.game.Game;
import mage.game.command.Commander;
import mage.game.stack.Spell;

import java.util.UUID;

/**
 * @author LevelX2
 */
public class FilteredSpellManaCondition extends ManaCondition {

    private final FilterTyped filter;
    private final String manaText;

    public FilteredSpellManaCondition(FilterTyped filter) {
        this.filter = filter == null ? null : filter.copy();
        this.manaText = filter == null ? "a spell" : filter.getMessage();
    }

    @Override
    public boolean apply(Game game, Ability source, UUID originalId, Cost costToPay) {
        if (!(source instanceof SpellAbility spellAbility) || source.isActivated()) {
            return false;
        }

        MageObject object = game.getObject(source);
        if (object == null) {
            return false;
        }

        if (object instanceof Spell stackObject) {
            return filter == null || filter.match(stackObject, source.getControllerId(), source, game);
        }

        if (game.inCheckPlayableState()) {
            Spell spell = null;
            if (object instanceof Card card) {
                spell = new Spell(card, spellAbility, source.getControllerId(), game.getState().getZone(source.getSourceId()), game);
            } else if (object instanceof Commander commander) {
                spell = new Spell(commander.getSourceObject(), spellAbility, source.getControllerId(), game.getState().getZone(source.getSourceId()), game);
            }
            return spell != null && (filter == null || filter.match(spell, source.getControllerId(), source, game));
        }

        return false;
    }

    @Override
    public String getManaText() {
        return manaText;
    }
}


