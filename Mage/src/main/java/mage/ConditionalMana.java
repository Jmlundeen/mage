package mage;

import mage.abilities.condition.Condition;
import mage.constants.ManaType;
import mage.filter.FilterMana;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * If subclassing and adding extra field, you must be sure to override equals() and hashCode to include the new fields.
 *
 * @author nantuko
 */
public class ConditionalMana extends Mana implements Serializable, Emptiable {

    /**
     * Text displayed as a description for conditional mana. Usually includes
     * the conditions that should be met to use this mana.
     */
    protected String staticText = "Conditional mana.";

    /**
     * UUID of source for mana.
     */
    private UUID manaProducerId;

    /**
     * UUID originalId of source ability for mana.
     */
    private UUID manaProducerOriginalId;

    public ConditionalMana(Mana mana) {
        super(mana);
    }

    protected ConditionalMana(final ConditionalMana conditionalMana) {
        super(conditionalMana);
        conditions.addAll(conditionalMana.conditions);
        comparisonScope = conditionalMana.comparisonScope;
        staticText = conditionalMana.staticText;
        manaProducerId = conditionalMana.manaProducerId;
        manaProducerOriginalId = conditionalMana.manaProducerOriginalId;
    }

    @Override
    public ConditionalMana copy() {
        return new ConditionalMana(this);
    }

    public String getDescription() {
        return staticText;
    }

    public void removeAll(FilterMana filter) {
        if (filter == null) {
            return;
        }
        if (filter.isBlack()) {
            black = 0;
        }
        if (filter.isBlue()) {
            blue = 0;
        }
        if (filter.isWhite()) {
            white = 0;
        }
        if (filter.isGreen()) {
            green = 0;
        }
        if (filter.isRed()) {
            red = 0;
        }
        if (filter.isColorless()) {
            colorless = 0;
        }
        if (filter.isGeneric()) {
            generic = 0;
        }
    }

    public UUID getManaProducerId() {
        return manaProducerId;
    }

    public void setManaProducerId(UUID manaProducerId) {
        this.manaProducerId = manaProducerId;
    }

    public UUID getManaProducerOriginalId() {
        return manaProducerOriginalId;
    }

    public void setManaProducerOriginalId(UUID manaProducerOriginalId) {
        this.manaProducerOriginalId = manaProducerOriginalId;
    }

    public void clear(ManaType manaType) {
        switch (manaType) {
            case BLACK:
                black = 0;
                break;
            case BLUE:
                blue = 0;
                break;
            case GREEN:
                green = 0;
                break;
            case RED:
                red = 0;
                break;
            case WHITE:
                white = 0;
                break;
            case GENERIC:
                generic = 0;
                break;
            case COLORLESS:
                colorless = 0;
                break;
        }
    }

    @Override
    public void add(Mana mana) {
        if (mana instanceof ConditionalMana) {
            for (Condition condition : mana.getConditions()) {
                addCondition(condition);
            }
        }
        super.add(mana);
    }

    public String getConditionString() {
        StringBuilder sb = new StringBuilder();

        sb.append('[');
        for (Condition condition : conditions) {
            sb.append('{');
            sb.append(condition.getManaText());
            sb.append('}');
        }
        sb.append(']');

        return sb.toString();
    }

    public void add(ManaType manaType, int amount) {
        switch (manaType) {
            case BLACK:
                black += amount;
                break;
            case BLUE:
                blue += amount;
                break;
            case GREEN:
                green += amount;
                break;
            case RED:
                red += amount;
                break;
            case WHITE:
                white += amount;
                break;
            case COLORLESS:
                colorless += amount;
                break;
            case GENERIC:
                generic += amount;
                break;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), conditions, staticText, comparisonScope, manaProducerId, manaProducerOriginalId);
    }

    @Override
    public boolean equals(Object o) {
        // Check Mana.equals(). If that's isn't equal no need to check further.
        if (!super.equals(o)) {
            return false;
        }
        ConditionalMana that = (ConditionalMana) o;

        if (!Objects.equals(this.staticText, that.staticText)) {
            return false;
        }
        if (!Objects.equals(this.manaProducerId, that.manaProducerId)) {
            return false;
        }
        if (!Objects.equals(this.manaProducerOriginalId, that.manaProducerOriginalId)) {
            return false;
        }
        if (!Objects.equals(this.comparisonScope, that.comparisonScope)) {
            return false;
        }
        if (this.conditions == null || that.conditions == null
                || this.conditions.size() != that.conditions.size()) {
            return false;
        }
        for (int i = 0; i < this.conditions.size(); i++) {
            if (!(Objects.equals(this.conditions.get(i), that.conditions.get(i)))) {
                return false;
            }
        }

        return true;
    }
}
