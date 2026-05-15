package mage.abilities.mana.conditional;

import mage.MageObject;
import mage.abilities.Ability;
import mage.abilities.SpellAbility;
import mage.abilities.costs.Cost;
import mage.cards.Card;
import mage.filter.FilterSpell;
import mage.game.Game;
import mage.game.command.Commander;
import mage.game.stack.Spell;
import mage.game.stack.StackObject;

import java.util.Objects;
import java.util.UUID;

/**
 * @author LevelX2
 */
public class FilteredSpellManaCondition extends ManaCondition {

    private final FilterSpell filter;
    private final String manaText;

    public FilteredSpellManaCondition(FilterSpell filter, String manaText) {
        this.filter = Objects.requireNonNull(filter);
        this.manaText = Objects.requireNonNull(manaText);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        if (!(source instanceof SpellAbility spellAbility) || source.isActivated()) {
            return false;
        }

        MageObject object = game.getObject(source);
        if (object instanceof StackObject stackObject) {
            return filter.match(stackObject, source.getControllerId(), source, game);
        }

        if (game.inCheckPlayableState()) {
            Spell spell = null;
            if (object instanceof Card card) {
                spell = new Spell(card, spellAbility, source.getControllerId(), game.getState().getZone(source.getSourceId()), game);
            } else if (object instanceof Commander commander) {
                spell = new Spell(commander.getSourceObject(), spellAbility, source.getControllerId(), game.getState().getZone(source.getSourceId()), game);
            }
            return spell != null && filter.match(spell, source.getControllerId(), source, game);
        }

        return false;
    }

    @Override
    public boolean apply(Game game, Ability source, UUID originalId, Cost costToPay) {
        return apply(game, source);
    }

    @Override
    public String getManaText() {
        return manaText;
    }
}


