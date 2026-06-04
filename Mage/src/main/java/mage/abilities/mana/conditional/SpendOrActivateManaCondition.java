package mage.abilities.mana.conditional;

import mage.MageObject;
import mage.abilities.Ability;
import mage.abilities.SpellAbility;
import mage.abilities.costs.Cost;
import mage.cards.Card;
import mage.constants.AbilityType;
import mage.filter.FilterTyped;
import mage.game.Game;
import mage.game.command.Commander;
import mage.game.stack.Spell;
import mage.game.stack.StackObject;

import java.util.UUID;

/**
 * Spend this mana only to cast matching spells or activate abilities of matching objects.
 */
public class SpendOrActivateManaCondition extends ManaCondition {

    private final FilterTyped filter;
    private final String manaText;

    public SpendOrActivateManaCondition(FilterTyped filter) {
        this.filter = filter == null ? null : filter.copy();
        this.manaText = filter != null ? filter.getMessage() : "a creature spell or activated ability of a creature or creature card";
    }

    public SpendOrActivateManaCondition() {
        this(null);
    }

    @Override
    public boolean apply(Game game, Ability source, UUID originalId, Cost costToPay) {
        if (game == null || source == null) {
            return false;
        }
        if (!(source.getAbilityType().isActivatedAbility() || source.getAbilityType() == AbilityType.SPELL)) {
            return false;
        }

        MageObject object = game.getObject(source);
        if (object == null) {
            return false;
        }

        // apply to spell or stack ability
        if (object instanceof StackObject stackObject) {
            return filter == null ? stackObject.isCreature(game) : filter.match(stackObject, source.getControllerId(), source, game);
        }

        // if not a stack object, check filter against a spell
        if (game.inCheckPlayableState() && source instanceof SpellAbility spellAbility) {
            Spell spell = null;
            if (object instanceof Card card) {
                spell = new Spell(card, spellAbility, source.getControllerId(), game.getState().getZone(source.getSourceId()), game);
            } else if (object instanceof Commander commander) {
                spell = new Spell(commander.getSourceObject(), spellAbility, source.getControllerId(), game.getState().getZone(source.getSourceId()), game);
            }
            return spell != null && (filter == null ? spell.isCreature(game) : filter.match(spell, source.getControllerId(), source, game));
        }

        // fallback to matching the source object or ability
        return filter == null ? object.isCreature(game) : filter.match(object, source.getControllerId(), source, game) || filter.match(source, source.getControllerId(), source, game);
    }

    @Override
    public String getManaText() {
        return manaText;
    }
}

