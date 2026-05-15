package mage.abilities.mana.value;

import mage.Mana;
import mage.abilities.Ability;
import mage.abilities.dynamicvalue.DynamicValue;
import mage.abilities.effects.Effect;
import mage.abilities.mana.ComposedManaEffect;
import mage.abilities.mana.providers.ManaTypeProvider;
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
    private final ManaTypeProvider manaTypeProvider;
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
        this.manaTypeProvider = null;
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
        this.manaTypeProvider = null;
        this.anyCombination = anyCombination;
    }

    public DynamicManaValue(DynamicValue amount, ManaTypeProvider manaTypeProvider, boolean anyCombination) {
        this(amount, manaTypeProvider, anyCombination, 0);
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
        this.manaTypeProvider = null;
        this.anyCombination = anyCombination;
    }

    public DynamicManaValue(DynamicValue amount, ManaTypeProvider manaTypeProvider, boolean anyCombination, int baseAmount) {
        this.amount = amount;
        this.singleType = null;
        this.baseAmount = baseAmount;
        this.choices = null;
        this.manaTypeProvider = manaTypeProvider;
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
        if (game == null) {
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
        Set<ManaType> currentChoices = getChoices(game, source, manaEffect);
        if (currentChoices != null) {
            if (currentChoices.isEmpty()) {
                return Collections.emptyList();
            }
            List<Mana> options = new ArrayList<>();
            
            if (anyCombination) {
                if (currentChoices.size() == 1) {
                    Mana mana = new Mana();
                    setMana(mana, currentChoices.iterator().next(), calculatedAmount);
                    return Collections.singletonList(mana);
                }
                if (produceMana) {
                    Player player = getChoicePlayer(game, source, manaEffect);
                    if (player == null) {
                        return Collections.emptyList();
                    }
                    List<String> choiceList = getChoiceStrings(currentChoices);
                    List<Integer> manaList = player.getMultiAmount(Outcome.PutManaInPool, choiceList, 0, calculatedAmount, calculatedAmount, MultiAmountType.MANA, game);
                    Mana mana = getMana(choiceList, manaList);
                    return Collections.singletonList(mana);
                }
                // Any combination - return single mana with all possible colors
                // The actual distribution will be chosen by player during resolution
                Mana mana = new Mana();
                for (ManaType type : currentChoices) {
                    setMana(mana, type, calculatedAmount);
                }
                mana.setAnyCombination(true);
                options.add(mana);
            } else {
                if (produceMana) {
                    Player player = getChoicePlayer(game, source, manaEffect);
                    if (player == null) {
                        return Collections.emptyList();
                    }
                    Choice choice = ManaType.getChoiceOfManaTypes(currentChoices, !currentChoices.contains(ManaType.COLORLESS));
                    if (choice.getChoices().size() == 1) {
                        choice.setChoice(choice.getChoices().iterator().next());
                    } else if (!player.choose(Outcome.PutManaInPool, choice, game)) {
                        return Collections.emptyList();
                    }
                    if (choice.getChoice() != null) {
                        ManaType chosenType = ManaType.findByName(choice.getChoice());
                        return Collections.singletonList(new Mana(chosenType, calculatedAmount));
                    }
                }
                // Choose one color - return one option per color
                for (ManaType type : currentChoices) {
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

    private Player getChoicePlayer(Game game, Ability source, Effect manaEffect) {
        if (manaEffect instanceof ComposedManaEffect composedManaEffect) {
            return composedManaEffect.getChoicePlayer(game, source);
        }
        return game.getPlayer(source.getControllerId());
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

    private @NonNull List<String> getChoiceStrings(Set<ManaType> manaChoices) {
        List<String> choiceList = new ArrayList<>();
        // ensure WUBRG order
        if (manaChoices.contains(ManaType.WHITE)) {
            choiceList.add("W");
        }
        if (manaChoices.contains(ManaType.BLUE)) {
            choiceList.add("U");
        }
        if (manaChoices.contains(ManaType.BLACK)) {
            choiceList.add("B");
        }
        if (manaChoices.contains(ManaType.RED)) {
            choiceList.add("R");
        }
        if (manaChoices.contains(ManaType.GREEN)) {
            choiceList.add("G");
        }
        return choiceList;
    }

    private Set<ManaType> getChoices(Game game, Ability source, Effect manaEffect) {
        if (choices != null) {
            return choices;
        }
        if (manaTypeProvider == null) {
            return null;
        }
        Set<ManaType> dynamicChoices = manaTypeProvider.getManaTypes(game, source, manaEffect);
        if (dynamicChoices == null || dynamicChoices.isEmpty()) {
            return Collections.emptySet();
        }
        return EnumSet.copyOf(dynamicChoices);
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
        if (manaTypeProvider != null) {
            return EnumSet.noneOf(ManaType.class);
        }
        return EnumSet.of(ManaType.COLORLESS);
    }

    @Override
    public Set<ManaType> getProducibleTypes(Game game, Ability source, Effect manaEffect) {
        Set<ManaType> currentChoices = getChoices(game, source, manaEffect);
        if (currentChoices == null || currentChoices.isEmpty()) {
            return getProducibleTypes();
        }
        Set<ManaType> types = EnumSet.copyOf(currentChoices);
        types.remove(ManaType.GENERIC);
        return types;
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

    public ManaTypeProvider getManaTypeProvider() {
        return manaTypeProvider;
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
        if (manaTypeProvider != null) {
            return new DynamicManaValue(amount.copy(), manaTypeProvider.copy(), anyCombination, baseAmount);
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
        } else if (choices != null || manaTypeProvider != null) {
            sb.append("from ").append(choices != null ? choices : "runtime mana types");
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
