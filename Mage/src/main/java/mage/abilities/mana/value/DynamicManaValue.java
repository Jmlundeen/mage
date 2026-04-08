package mage.abilities.mana.value;

import mage.Mana;
import mage.abilities.Ability;
import mage.abilities.dynamicvalue.DynamicValue;
import mage.abilities.effects.Effect;
import mage.choices.Choice;
import mage.constants.ManaType;
import mage.constants.MultiAmountType;
import mage.constants.Outcome;
import mage.game.Game;
import mage.players.Player;
import org.jspecify.annotations.NonNull;

import java.util.*;

/**
 * A dynamic mana value that calculates the amount based on game state.
 * The amount is recalculated each time evaluate is called.
 * 
 * <p>Supports three modes:
 * <ul>
 *   <li>Single type - produces a fixed color (e.g., all green)</li>
 *   <li>Choice - player chooses one color during activation</li>
 *   <li>Combination - player chooses any combination of colors (e.g., any amount of B and/or R)</li>
 * </ul>
 */
public class DynamicManaValue implements ManaValue {

    private final DynamicValue amount;
    private final ManaType singleType;
    private final int baseAmount;
    private final Set<ManaType> choices;
    private final boolean anyCombination;

    /**
     * Creates a dynamic mana value that produces mana of the specified type.
     * 
     * @param amount the dynamic value that calculates how much mana to produce
     * @param manaType the type of mana to produce (WHITE, BLUE, BLACK, RED, GREEN, COLORLESS)
     */
    public DynamicManaValue(DynamicValue amount, ManaType manaType) {
        this(amount, manaType, 0);
    }

    /**
     * Creates a dynamic mana value with a base amount plus a dynamic component.
     * 
     * @param amount the dynamic value that calculates the additional mana
     * @param manaType the type of mana to produce
     * @param baseAmount a fixed base amount to add
     */
    public DynamicManaValue(DynamicValue amount, ManaType manaType, int baseAmount) {
        this.amount = amount;
        this.singleType = manaType;
        this.baseAmount = baseAmount;
        this.choices = null;
        this.anyCombination = false;
    }

    /**
     * Creates a dynamic mana value where the player chooses one color from the given set.
     * 
     * @param amount the dynamic value that calculates how much mana to produce
     * @param choices the set of colors the player can choose from
     */
    public DynamicManaValue(DynamicValue amount, Set<ManaType> choices) {
        this(amount, choices, false);
    }

    /**
     * Creates a dynamic mana value with color choices.
     * 
     * @param amount the dynamic value that calculates how much mana to produce
     * @param choices the set of colors to choose from
     * @param anyCombination if true, player can choose any combination of the colors;
     *                       if false, player chooses exactly one color
     */
    public DynamicManaValue(DynamicValue amount, Set<ManaType> choices, boolean anyCombination) {
        this.amount = amount;
        this.singleType = null;
        this.baseAmount = 0;
        this.choices = EnumSet.copyOf(choices);
        this.anyCombination = anyCombination;
    }

    /**
     * Creates a dynamic mana value with a base amount and color choices.
     * 
     * @param amount the dynamic value that calculates how much mana to produce
     * @param choices the set of colors to choose from
     * @param anyCombination if true, player can choose any combination of the colors
     * @param baseAmount a fixed base amount to add
     */
    public DynamicManaValue(DynamicValue amount, Set<ManaType> choices, boolean anyCombination, int baseAmount) {
        this.amount = amount;
        this.singleType = null;
        this.baseAmount = baseAmount;
        this.choices = EnumSet.copyOf(choices);
        this.anyCombination = anyCombination;
    }

    private int calculateAmount(Game game, Ability source, Effect manaEffect) {
        int calculatedAmount = baseAmount;
        if (game != null && amount != null) {
            calculatedAmount += amount.calculate(game, source, manaEffect);
        }
        return calculatedAmount;
    }

    @Override
    public List<Mana> evaluate(Game game, Ability source, Effect manaEffect, boolean produceMana) {
        Player player = game.getPlayer(source.getControllerId());
        if (player == null) {
            return Collections.emptyList();
        }
        int calculatedAmount = calculateAmount(game, source, manaEffect);
        if (calculatedAmount <= 0) {
            return Collections.emptyList();
        }

        // Single type mode
        if (singleType != null) {
            Mana mana = new Mana();
            setMana(mana, singleType, calculatedAmount);
            return Collections.singletonList(mana);
        }

        // Choice mode - return all possible options
        if (choices != null) {
            List<Mana> options = new ArrayList<>();
            
            if (anyCombination) {
                if (produceMana) {
                    List<String> choiceList = getChoiceStrings();
                    List<Integer> manaList = player.getMultiAmount(Outcome.PutManaInPool, choiceList, 0, calculatedAmount, calculatedAmount, MultiAmountType.MANA, game);
                    Mana mana = getMana(choiceList, manaList);
                    return Collections.singletonList(mana);
                }
                // Any combination - return single mana with all possible colors
                // The actual distribution will be chosen by player during resolution
                Mana mana = new Mana();
                for (ManaType type : choices) {
                    setMana(mana, type, calculatedAmount);
                }
                options.add(mana);
            } else {
                if (produceMana) {
                    Choice choice = ManaType.getChoiceOfManaTypes(choices, !choices.contains(ManaType.COLORLESS));
                    if (player.choose(Outcome.PutManaInPool, choice, game)) {
                        if (choice.getChoice() != null) {
                            ManaType chosenType = ManaType.findByName(choice.getChoice());
                            return Collections.singletonList(new Mana(chosenType, calculatedAmount));
                        }
                    }
                }
                // Choose one color - return one option per color
                for (ManaType type : choices) {
                    if (type == ManaType.COLORLESS || type == ManaType.GENERIC) {
                        options.add(Mana.ColorlessMana(calculatedAmount));
                    } else {
                        switch (type) {
                            case WHITE -> options.add(Mana.WhiteMana(calculatedAmount));
                            case BLUE -> options.add(Mana.BlueMana(calculatedAmount));
                            case BLACK -> options.add(Mana.BlackMana(calculatedAmount));
                            case RED -> options.add(Mana.RedMana(calculatedAmount));
                            case GREEN -> options.add(Mana.GreenMana(calculatedAmount));
                        }
                    }
                }
            }
            return options;
        }

        // Default: colorless
        return Collections.singletonList(Mana.ColorlessMana(calculatedAmount));
    }

    private static @NonNull Mana getMana(List<String> choiceList, List<Integer> manaList) {
        Mana mana = new Mana();
        for (int i = 0; i < choiceList.size(); i++) {
            switch (choiceList.get(i)) {
                case "W" -> mana.setWhite(manaList.get(i));
                case "U" -> mana.setBlue(manaList.get(i));
                case "B" -> mana.setBlack(manaList.get(i));
                case "R" -> mana.setRed(manaList.get(i));
                case "G" -> mana.setGreen(manaList.get(i));
                case "Colorless" -> mana.setColorless(manaList.get(i));
            }
        }
        return mana;
    }

    private @NonNull List<String> getChoiceStrings() {
        List<String> choiceList = new ArrayList<>();
        for (ManaType type : choices) {
            switch (type) {
                case WHITE -> choiceList.add("W");
                case BLUE -> choiceList.add("U");
                case BLACK -> choiceList.add("B");
                case RED -> choiceList.add("R");
                case GREEN -> choiceList.add("G");
                case COLORLESS -> choiceList.add("Colorless");
            }
        }
        return choiceList;
    }

    private void setMana(Mana mana, ManaType type, int calculatedAmount) {
        switch (type) {
            case WHITE -> mana.setWhite(calculatedAmount);
            case BLUE -> mana.setBlue(calculatedAmount);
            case BLACK -> mana.setBlack(calculatedAmount);
            case RED -> mana.setRed(calculatedAmount);
            case GREEN -> mana.setGreen(calculatedAmount);
            case COLORLESS -> mana.setColorless(calculatedAmount);
            case GENERIC -> mana.setGeneric(calculatedAmount);
        }
    }

    @Override
    public Set<ManaType> getProducibleTypes() {
        if (singleType != null) {
            if (singleType == ManaType.GENERIC) {
                return EnumSet.of(ManaType.COLORLESS);
            }
            return EnumSet.of(singleType);
        }
        if (choices != null) {
            Set<ManaType> types = EnumSet.copyOf(choices);
            types.remove(ManaType.GENERIC);
            return types;
        }
        return EnumSet.of(ManaType.COLORLESS);
    }

    public DynamicValue getAmount() {
        return amount;
    }

    public ManaType getSingleType() {
        return singleType;
    }

    public Set<ManaType> getChoices() {
        return choices;
    }

    public boolean isAnyCombination() {
        return anyCombination;
    }

    public int getBaseAmount() {
        return baseAmount;
    }

    @Override
    public DynamicManaValue copy() {
        if (singleType != null) {
            return new DynamicManaValue(amount.copy(), singleType, baseAmount);
        }
        if (choices != null) {
            return new DynamicManaValue(amount.copy(), choices, anyCombination, baseAmount);
        }
        return new DynamicManaValue(amount, EnumSet.of(ManaType.COLORLESS), false, baseAmount);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (baseAmount > 0) {
            sb.append(baseAmount).append(" + ");
        }
        sb.append('{').append(amount.getMessage()).append("} ");
        if (singleType != null) {
            sb.append(singleType.name().toLowerCase());
        } else if (choices != null) {
            sb.append("from ").append(choices);
            if (anyCombination) {
                sb.append(" (any combination)");
            }
        }
        return sb.toString();
    }

    /**
     * Creates a dynamic mana value for "any combination of colors" (e.g., all five colors).
     * 
     * @param amount the dynamic value that calculates how much mana to produce
     * @return dynamic mana value with any combination of the five colors
     */
    public static DynamicManaValue anyCombination(DynamicValue amount) {
        return new DynamicManaValue(amount, 
            EnumSet.of(ManaType.WHITE, ManaType.BLUE, ManaType.BLACK, ManaType.RED, ManaType.GREEN), 
            true);
    }

    /**
     * Creates a dynamic mana value for a specific set of colors in combination.
     * Example: {B} and/or {R}
     * 
     * @param amount the dynamic value that calculates how much mana to produce
     * @param colors the colors that can be combined
     * @return dynamic mana value with the specified combination
     */
    public static DynamicManaValue combination(DynamicValue amount, Set<ManaType> colors) {
        return new DynamicManaValue(amount, colors, true);
    }
}
