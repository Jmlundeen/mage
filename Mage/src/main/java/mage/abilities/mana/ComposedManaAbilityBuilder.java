package mage.abilities.mana;

import mage.Mana;
import mage.abilities.condition.Condition;
import mage.abilities.costs.Cost;
import mage.abilities.dynamicvalue.DynamicValue;
import mage.abilities.dynamicvalue.common.StaticValue;
import mage.abilities.effects.mana.ManaEffect;
import mage.abilities.mana.providers.ManaPlayerProvider;
import mage.abilities.mana.providers.ManaTypeProvider;
import mage.abilities.mana.value.*;
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
    private Condition activationCondition = null;
    private ManaPlayerProvider manaPlayerProvider = null;
    private Cost anyPlayerPaysCost = null;
    private String anyPlayerPaysChooseUseText = null;
    private ManaEffect manaEffect = null;

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

    public ComposedManaAbilityBuilder addStatic(Set<ManaType> manaTypes, int amount) {
        manaValues.add(new StaticManaValue(manaTypes, amount));
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

    public ComposedManaAbilityBuilder addDynamic(DynamicValue amount, Set<ManaType> manaTypes) {
        manaValues.add(new DynamicManaValue(amount, manaTypes));
        return this;
    }

    /**
     * Adds mana that gives the same amount to each mana type in the given set.
     * @param amount the dynamic value that calculates how much of each type to produce
     * @param manaTypes the set of mana types to produce
     */
    public ComposedManaAbilityBuilder addDynamicEach(DynamicValue amount, Set<ManaType> manaTypes) {
        manaValues.add(new EachManaTypeManaValue(amount, manaTypes));
        return this;
    }

    /**
     * Adds mana that gives the same amount to each runtime-provided mana type.
     * @param amount the dynamic value that calculates how much of each type to produce
     * @param manaTypeProvider the provider that supplies the mana types to produce
     */
    public ComposedManaAbilityBuilder addDynamicEach(DynamicValue amount, ManaTypeProvider manaTypeProvider) {
        manaValues.add(new EachManaTypeManaValue(amount, manaTypeProvider));
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
     * Adds a dynamic mana value where player chooses one mana type from runtime-provided options.
     */
    public ComposedManaAbilityBuilder addDynamicChoice(DynamicValue amount, ManaTypeProvider choicesProvider) {
        manaValues.add(new DynamicManaValue(amount, choicesProvider, false));
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
     * Adds a dynamic mana value where player chooses any combination from runtime-provided mana types.
     */
    public ComposedManaAbilityBuilder addDynamicCombination(DynamicValue amount, ManaTypeProvider choicesProvider) {
        manaValues.add(new DynamicManaValue(amount, choicesProvider, true));
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
     * Adds mana equal to the current mana in the player's mana pool.
     */
    public ComposedManaAbilityBuilder addCurrentManaPool() {
        manaValues.add(new CurrentManaPoolManaValue());
        return this;
    }

    /**
     * Adds mana equal to the current mana in the player's mana pool multiplied by the given amount.
     */
    public ComposedManaAbilityBuilder addCurrentManaPool(int multiplier) {
        manaValues.add(new CurrentManaPoolManaValue(multiplier));
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
     * Adds a static mana value where player chooses from runtime-provided mana types.
     */
    public ComposedManaAbilityBuilder addChoice(ManaTypeProvider manaTypeProvider, int amount) {
        manaValues.add(new DynamicManaValue(StaticValue.get(amount), manaTypeProvider, false));
        return this;
    }

    /**
     * Adds mana that gives the same fixed amount to each mana type in the given set.
     */
    public ComposedManaAbilityBuilder addEach(Set<ManaType> manaTypes, int amount) {
        return addDynamicEach(StaticValue.get(amount), manaTypes);
    }

    /**
     * Adds mana that gives the same fixed amount to each runtime-provided mana type.
     */
    public ComposedManaAbilityBuilder addEach(ManaTypeProvider manaTypeProvider, int amount) {
        return addDynamicEach(StaticValue.get(amount), manaTypeProvider);
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

    public ComposedManaAbilityBuilder activationCondition(Condition condition) {
        this.activationCondition = condition;
        return this;
    }

    /**
     * Sets provider for player who receives produced mana and makes any mana choices.
     */
    public ComposedManaAbilityBuilder playerProvider(ManaPlayerProvider manaPlayerProvider) {
        this.manaPlayerProvider = manaPlayerProvider;
        return this;
    }

    /**
     * Prevents mana production unless any player pays the given cost.
     */
    public ComposedManaAbilityBuilder addAnyPlayerPaysCost(Cost cost) {
        return addAnyPlayerPaysCost(cost, "Pay " + cost.getText() + " to prevent mana adding from {this}.");
    }

    /**
     * Prevents mana production unless any player pays the given cost.
     */
    public ComposedManaAbilityBuilder addAnyPlayerPaysCost(Cost cost, String chooseUseText) {
        this.anyPlayerPaysCost = cost;
        this.anyPlayerPaysChooseUseText = chooseUseText;
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
        this.manaEffect = buildEffect();
        return new ComposedManaAbility(this);
    }

    public ManaEffect buildEffect() {
        if (manaValues.isEmpty()) {
            throw new IllegalStateException("At least one mana value must be added");
        }
        ManaEffect effect = new ComposedManaEffect(
                manaValues,
                spendingConditions,
                manaPlayerProvider,
                anyPlayerPaysCost,
                anyPlayerPaysChooseUseText
        );
        if (ruleText != null) {
            effect.setText(ruleText);
        }
        this.manaEffect = effect;
        return effect;
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

    public Condition getActivationCondition() {
        return activationCondition;
    }

    public ManaEffect getManaEffect() {
        return manaEffect;
    }

    public ManaPlayerProvider getManaPlayerProvider() {
        return manaPlayerProvider;
    }
}
