package mage.abilities.mana.value;

import mage.Mana;
import mage.abilities.Ability;
import mage.abilities.dynamicvalue.DynamicValue;
import mage.abilities.effects.Effect;
import mage.abilities.mana.providers.ManaTypeProvider;
import mage.constants.ManaType;
import mage.game.Game;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Produces same amount of mana for each mana type in a static or runtime-provided set.
 * Example: if amount is 2 and mana types are {W}{U}{B}, this produces {W}{W}{U}{U}{B}{B}.
 */
public class EachManaTypeManaValue implements ManaValue {

    private final DynamicValue amount;
    private final Set<ManaType> manaTypes;
    private final ManaTypeProvider manaTypeProvider;

    public EachManaTypeManaValue(DynamicValue amount, Set<ManaType> manaTypes) {
        this.amount = amount;
        this.manaTypes = manaTypes.isEmpty() ? EnumSet.noneOf(ManaType.class) : EnumSet.copyOf(manaTypes);
        this.manaTypeProvider = null;
    }

    public EachManaTypeManaValue(DynamicValue amount, ManaTypeProvider manaTypeProvider) {
        this.amount = amount;
        this.manaTypes = null;
        this.manaTypeProvider = manaTypeProvider;
    }

    @Override
    public List<Mana> evaluate(Game game, Ability source, Effect manaEffect, boolean produceMana) {
        int calculatedAmount = calculateAmount(game, source, manaEffect, produceMana, amount);
        if (calculatedAmount <= 0) {
            return Collections.emptyList();
        }

        Set<ManaType> currentManaTypes = getManaTypes(game, source, manaEffect);
        if (currentManaTypes.isEmpty()) {
            return Collections.emptyList();
        }

        Mana mana = new Mana();
        for (ManaType manaType : currentManaTypes) {
            setMana(mana, manaType, calculatedAmount);
        }
        return Collections.singletonList(mana);
    }


    private Set<ManaType> getManaTypes(Game game, Ability source, Effect manaEffect) {
        if (manaTypes != null) {
            return manaTypes;
        }
        if (manaTypeProvider == null) {
            return Collections.emptySet();
        }
        Set<ManaType> dynamicTypes = manaTypeProvider.getManaTypes(game, source, manaEffect);
        return dynamicTypes == null || dynamicTypes.isEmpty()
                ? Collections.emptySet()
                : EnumSet.copyOf(dynamicTypes);
    }

    private static void setMana(Mana mana, ManaType manaType, int amount) {
        switch (manaType) {
            case WHITE -> mana.setWhite(amount);
            case BLUE -> mana.setBlue(amount);
            case BLACK -> mana.setBlack(amount);
            case RED -> mana.setRed(amount);
            case GREEN -> mana.setGreen(amount);
            case COLORLESS -> mana.setColorless(amount);
            case GENERIC -> mana.setGeneric(amount);
        }
    }

    @Override
    public Set<ManaType> getProducibleTypes() {
        if (manaTypes == null || manaTypes.isEmpty()) {
            return EnumSet.noneOf(ManaType.class);
        }
        Set<ManaType> types = EnumSet.copyOf(manaTypes);
        types.remove(ManaType.GENERIC);
        return types;
    }

    @Override
    public Set<ManaType> getProducibleTypes(Game game, Ability source, Effect manaEffect) {
        Set<ManaType> currentManaTypes = getManaTypes(game, source, manaEffect);
        if (currentManaTypes.isEmpty()) {
            return getProducibleTypes();
        }
        Set<ManaType> types = EnumSet.copyOf(currentManaTypes);
        types.remove(ManaType.GENERIC);
        return types;
    }

    @Override
    public EachManaTypeManaValue copy() {
        DynamicValue amountCopy = amount == null ? null : amount.copy();
        return manaTypes != null
                ? new EachManaTypeManaValue(amountCopy, manaTypes)
                : new EachManaTypeManaValue(amountCopy, manaTypeProvider.copy());
    }

    @Override
    public String toString() {
        return '{' + (amount == null ? "0" : amount.getMessage()) + "} of each "
                + (manaTypes != null ? manaTypes : "runtime mana type");
    }
}

