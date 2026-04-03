package mage.abilities.mana;

import mage.Mana;
import mage.abilities.condition.Condition;
import mage.abilities.costs.Cost;
import mage.abilities.dynamicvalue.DynamicValue;
import mage.abilities.dynamicvalue.common.StaticValue;
import mage.abilities.mana.value.AnyColorManaValue;
import mage.abilities.mana.value.DynamicManaValue;
import mage.abilities.mana.value.ManaValue;
import mage.abilities.mana.value.StaticManaValue;
import mage.constants.ManaType;
import mage.constants.Zone;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Fluent builder for creating {@link ComposedManaAbility} instances.
 * 
 * <p>Example usage:
 * <pre>{@code
 * // Basic land ability: {T}: Add {W}
 * new ComposedManaAbilityBuilder()
 *     .addStatic(1, 0, 0, 0, 0, 0, 0)
 *     .cost(new TapSourceCost())
 *     .build();
 * 
 * // Dynamic based on creatures: {T}: Add X, where X = creatures you control
 * new ComposedManaAbilityBuilder()
 *     .addDynamic(new ControlledCreaturesCount(), ManaType.GREEN)
 *     .cost(new TapSourceCost())
 *     .build();
 * 
 * // Any color: {T}: Add one mana of any color
 * new ComposedManaAbilityBuilder()
 *     .addAnyColor(1)
 *     .cost(new TapSourceCost())
 *     .build();
 * 
 * // Conditional: can only cast creatures
 * new ComposedManaAbilityBuilder()
 *     .addStatic(0, 0, 0, 0, 1, 0)
 *     .condition(CreatureCastManaCondition.INSTANCE)
 *     .cost(new TapSourceCost())
 *     .build();
 * 
 * // Limited activations: activate only 3 times per turn
 * new ComposedManaAbilityBuilder()
 *     .addStatic(0, 1, 0, 0, 0, 0)
 *     .cost(new TapSourceCost())
 *     .maxActivations(3)
 *     .build();
 * }</pre>
 */
public class ComposedManaAbilityBuilder {

    private final List<ManaValue> manaValues = new ArrayList<>();
    private final List<Condition> spendingConditions = new ArrayList<>();
    private int maxActivations = Integer.MAX_VALUE;
    private boolean poolDependant = false;
    private Cost cost = null;
    private Zone zone = Zone.BATTLEFIELD;
    private String ruleText = null;

    public ComposedManaAbilityBuilder() {
    }

    /**
     * Adds a static mana value with individual color amounts.
     * 
     * @param white white mana amount
     * @param blue blue mana amount
     * @param black black mana amount
     * @param red red mana amount
     * @param green green mana amount
     * @param colorless colorless mana amount
     * @param generic generic mana amount
     * @return this builder
     */
    public ComposedManaAbilityBuilder addStatic(int white, int blue, int black, int red, int green, int colorless, int generic) {
        return addStatic(white, blue, black, red, green, colorless, generic, 0);
    }

    /**
     * Adds a static mana value with individual color amounts.
     * 
     * @param white white mana amount
     * @param blue blue mana amount
     * @param black black mana amount
     * @param red red mana amount
     * @param green green mana amount
     * @param colorless colorless mana amount
     * @param generic generic mana amount
     * @param any any-color mana amount
     * @return this builder
     */
    public ComposedManaAbilityBuilder addStatic(int white, int blue, int black, int red, int green, int colorless, int generic, int any) {
        manaValues.add(new StaticManaValue(white, blue, black, red, green, generic, colorless, any));
        return this;
    }

    /**
     * Adds a static mana value from a Mana object.
     * 
     * @param mana the mana to produce
     * @return this builder
     */
    public ComposedManaAbilityBuilder addStatic(Mana mana) {
        manaValues.add(new StaticManaValue(mana));
        return this;
    }

    /**
     * Adds a dynamic mana value that calculates the amount based on game state.
     * 
     * @param amount the dynamic value that calculates how much mana to produce
     * @param manaType the type of mana to produce
     * @return this builder
     */
    public ComposedManaAbilityBuilder addDynamic(DynamicValue amount, ManaType manaType) {
        manaValues.add(new DynamicManaValue(amount, manaType));
        return this;
    }

    /**
     * Adds a dynamic mana value with a base amount plus a dynamic component.
     * 
     * @param amount the dynamic value that calculates the additional mana
     * @param manaType the type of mana to produce
     * @param baseAmount a fixed base amount to add
     * @return this builder
     */
    public ComposedManaAbilityBuilder addDynamic(DynamicValue amount, ManaType manaType, int baseAmount) {
        manaValues.add(new DynamicManaValue(amount, manaType, baseAmount));
        return this;
    }

    /**
     * Adds a dynamic mana value where player chooses one color from the given set.
     * Example: "Add X mana. You may choose one of {B} or {R}."
     * 
     * @param amount the dynamic value that calculates how much mana to produce
     * @param choices the set of colors the player can choose from
     * @return this builder
     */
    public ComposedManaAbilityBuilder addDynamicChoice(DynamicValue amount, Set<ManaType> choices) {
        manaValues.add(new DynamicManaValue(amount, choices, false));
        return this;
    }

    /**
     * Adds a dynamic mana value where player chooses any combination of colors.
     * Example: "Add X mana in any combination of {B} and/or {R}."
     * 
     * @param amount the dynamic value that calculates how much mana to produce
     * @param choices the set of colors that can be combined
     * @return this builder
     */
    public ComposedManaAbilityBuilder addDynamicCombination(DynamicValue amount, Set<ManaType> choices) {
        manaValues.add(new DynamicManaValue(amount, choices, true));
        return this;
    }

    /**
     * Adds a dynamic mana value where player chooses any combination of all five colors.
     * Example: "Add X mana in any combination of colors."
     * 
     * @param amount the dynamic value that calculates how much mana to produce
     * @return this builder
     */
    public ComposedManaAbilityBuilder addDynamicAnyCombination(DynamicValue amount) {
        manaValues.add(DynamicManaValue.anyCombination(amount));
        return this;
    }

    /**
     * Adds an "any color" mana value - produces mana that can be spent as any color.
     * 
     * @param amount the amount of any-color mana
     * @return this builder
     */
    public ComposedManaAbilityBuilder addAnyColor(int amount) {
        manaValues.add(new AnyColorManaValue(amount));
        return this;
    }

    /**
     * Adds a choice mana value where player chooses the color during activation.
     * 
     * @param choices the set of colors the player can choose from
     * @param amount the amount of mana of the chosen color
     * @return this builder
     */
    public ComposedManaAbilityBuilder addChoice(Set<ManaType> choices, int amount) {
        manaValues.add(new DynamicManaValue(StaticValue.get(amount), choices, false));
        return this;
    }

    /**
     * Adds a choice mana value where player chooses any one color.
     * 
     * @param amount the amount of mana of the chosen color
     * @return this builder
     */
    public ComposedManaAbilityBuilder addChoiceAnyColor(int amount) {
        return addChoice(EnumSet.of(ManaType.WHITE, ManaType.BLUE, ManaType.BLACK, ManaType.RED, ManaType.GREEN), amount);
    }

    /**
     * Adds a spending condition to restrict when the produced mana can be spent.
     * 
     * @param condition the condition that must be met to spend the mana
     * @return this builder
     */
    public ComposedManaAbilityBuilder condition(Condition condition) {
        spendingConditions.add(condition);
        return this;
    }

    /**
     * Sets the activation cost for this ability.
     * 
     * @param cost the activation cost
     * @return this builder
     */
    public ComposedManaAbilityBuilder cost(Cost cost) {
        this.cost = cost;
        return this;
    }

    /**
     * Sets the maximum number of times this ability can be activated per turn.
     * 
     * @param max the maximum activations per turn
     * @return this builder
     */
    public ComposedManaAbilityBuilder maxActivations(int max) {
        this.maxActivations = max;
        return this;
    }

    /**
     * Marks this ability as pool-dependent - the amount produced depends on 
     * mana already in the pool (e.g., Doubling Cube).
     * 
     * @return this builder
     */
    public ComposedManaAbilityBuilder poolDependant() {
        this.poolDependant = true;
        return this;
    }

    /**
     * Sets the zone where this ability can be activated.
     * Defaults to Zone.BATTLEFIELD.
     * 
     * @param zone the zone
     * @return this builder
     */
    public ComposedManaAbilityBuilder zone(Zone zone) {
        this.zone = zone;
        return this;
    }

    /**
     * Sets custom rule text for this ability. If not set, a default will be generated.
     *
     * @param ruleText the custom rule text
     * @return this builder
     */
    public ComposedManaAbilityBuilder ruleText(String ruleText) {
        this.ruleText = ruleText;
        return this;
    }

    /**
     * Builds the ComposedManaAbility.
     * 
     * @return the composed mana ability
     * @throws IllegalStateException if no mana values were added
     */
    public ComposedManaAbility build() {
        if (manaValues.isEmpty()) {
            throw new IllegalStateException("At least one mana value must be added");
        }
        return new ComposedManaAbility(this);
    }

    // Convenience static factory method
    public static ComposedManaAbilityBuilder builder() {
        return new ComposedManaAbilityBuilder();
    }

    public List<ManaValue> getManaValues() {
        return manaValues;
    }

    public List<Condition> getSpendingConditions() {
        return spendingConditions;
    }

    public int getMaxActivations() {
        return maxActivations;
    }

    public boolean isPoolDependant() {
        return poolDependant;
    }

    public Cost getCost() {
        return cost;
    }

    public Zone getZone() {
        return zone;
    }

    public String getRuleText() {
        return ruleText;
    }
}
