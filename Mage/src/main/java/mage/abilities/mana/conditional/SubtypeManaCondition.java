package mage.abilities.mana.conditional;

import mage.MageObject;
import mage.abilities.Ability;
import mage.abilities.costs.Cost;
import mage.constants.SubType;
import mage.filter.FilterTyped;
import mage.game.Game;

import java.util.Objects;
import java.util.UUID;

/**
 * Spend mana only on objects with snapshotted subtype.
 */
public class SubtypeManaCondition extends ManaCondition {

    private final SubType subType;
    private final String manaText;
    private final FilterTyped filter;

    public SubtypeManaCondition(SubType subType, FilterTyped filter) {
        this(subType, filter, subType == null ? "chosen subtype" : "type " + subType);
    }

    public SubtypeManaCondition(SubType subType, FilterTyped filter, String manaText) {
        this.subType = subType;
        this.manaText = manaText;
        this.filter = filter;
    }

    @Override
    public boolean apply(Game game, Ability source, UUID originalId, Cost costToPay) {
        if (game == null || source == null || subType == null) {
            return false;
        }
        MageObject object = game.getObject(source);
        if (object == null || !object.hasSubtype(subType, game)) {
            return false;
        }
        return filter == null || filter.match(source, source.getControllerId(), source, game);
    }

    @Override
    public String getManaText() {
        return manaText;
    }

    @Override
    public int hashCode() {
        return Objects.hash(subType, manaText);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof SubtypeManaCondition that)) {
            return false;
        }
        return subType == that.subType && Objects.equals(manaText, that.manaText);
    }
}

