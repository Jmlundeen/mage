package mage.abilities.mana;

import mage.Mana;
import mage.abilities.Ability;
import mage.abilities.condition.Condition;
import mage.abilities.effects.mana.ManaEffect;
import mage.abilities.mana.value.ManaValue;
import mage.constants.ManaType;
import mage.game.Game;
import mage.players.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * A consolidated activated mana ability that composes multiple mana values together.
 * 
 * <p>This class replaces multiple legacy mana ability classes:
 * <ul>
 *   <li>SimpleManaAbility</li>
 *   <li>DynamicManaAbility</li>
 *   <li>ConditionalManaAbility</li>
 *   <li>LimitedTimesPerTurnActivatedManaAbility</li>
 *   <li>AnyColorManaAbility</li>
 * </ul>
 * 
 * <p>Use {@link ComposedManaAbilityBuilder} to create instances.
 */
public class ComposedManaAbility extends ActivatedManaAbilityImpl {

    private final List<ManaValue> manaValues = new ArrayList<>();
    private final List<Condition> spendingConditions = new ArrayList<>();
    private final ComposedManaEffect manaEffect;

    ComposedManaAbility(ComposedManaAbilityBuilder builder) {
        super(builder.getZone(), null, builder.getCost());
        this.manaValues.addAll(builder.getManaValues());
        this.spendingConditions.addAll(builder.getSpendingConditions());
        this.maxActivationsPerTurn = builder.getMaxActivations();
        this.poolDependant = builder.isPoolDependant();
        this.manaEffect = new ComposedManaEffect();
        if (builder.getRuleText() != null) {
            this.manaEffect.setText(builder.getRuleText());
        }
        this.addEffect(this.manaEffect);
    }

    private ComposedManaAbility(final ComposedManaAbility ability) {
        super(ability);
        for (ManaValue mv : ability.manaValues) {
            this.manaValues.add(mv.copy());
        }
        this.spendingConditions.addAll(ability.spendingConditions);
        this.manaEffect = new ComposedManaEffect(ability.manaEffect);
    }

    @Override
    public ComposedManaAbility copy() {
        return new ComposedManaAbility(this);
    }

    @Override
    public List<Mana> getNetMana(Game game) {
        if (game != null && game.inCheckPlayableState()) {
            List<Mana> allMana = new ArrayList<>();
            for (ManaValue mv : manaValues) {
                List<Mana> evaluated = mv.evaluate(game, this, manaEffect);
                if (evaluated != null) {
                    for (Mana m : evaluated) {
                        // Attach conditions to each mana option for playable calculations
                        if (!spendingConditions.isEmpty()) {
                            m.addConditions(spendingConditions);
                            m.setConditionText(buildConditionText());
                        }
                        allMana.add(m);
                    }
                }
            }
            return allMana.isEmpty() ? super.getNetMana(game) : allMana;
        }
        return super.getNetMana(game);
    }

    @Override
    public List<Mana> getNetMana(Game game, Mana possibleManaInPool) {
        if (isPoolDependant()) {
            List<Mana> allMana = new ArrayList<>();
            for (ManaValue mv : manaValues) {
                List<Mana> evaluated = mv.evaluate(game, this, manaEffect);
                if (evaluated != null) {
                    for (Mana m : evaluated) {
                        // Attach conditions to each mana option for playable calculations
                        if (!spendingConditions.isEmpty()) {
                            m.addConditions(spendingConditions);
                            m.setConditionText(buildConditionText());
                        }
                        allMana.add(m);
                    }
                }
            }
            return allMana;
        }
        return getNetMana(game);
    }

    @Override
    public Set<ManaType> getProducableManaTypes(Game game) {
        Set<ManaType> types = java.util.HashSet.newHashSet(5);
        for (ManaValue mv : manaValues) {
            types.addAll(mv.getProducibleTypes());
        }
        return types.isEmpty() ? super.getProducableManaTypes(game) : types;
    }

    @Override
    public boolean definesMana(Game game) {
        return !getNetMana(game).isEmpty();
    }

    public List<ManaValue> getManaValues() {
        return manaValues;
    }

    public List<Condition> getSpendingConditions() {
        return spendingConditions;
    }

    public boolean hasSpendingConditions() {
        return !spendingConditions.isEmpty();
    }

    List<Mana> evaluateManaValues(Game game, Ability source) {
        List<Mana> allMana = new ArrayList<>();
        for (ManaValue mv : manaValues) {
            List<Mana> evaluated = mv.evaluate(game, source, manaEffect);
            if (evaluated != null) {
                allMana.addAll(evaluated);
            }
        }
        return allMana;
    }

    private String buildConditionText() {
        StringBuilder sb = new StringBuilder();
        for (Condition condition : spendingConditions) {
            if (!sb.isEmpty()) sb.append(", ");
            sb.append(condition.getManaText());
        }
        return sb.toString();
    }

    @Override
    public String getRule() {
        StringBuilder sb = new StringBuilder();
        if (!getCosts().isEmpty()) {
            if (!sb.isEmpty()) {
                sb.append(", ");
            }
            sb.append(getCosts().getText());
        }
        if (!sb.isEmpty()) {
            sb.append(": ");
        }
        sb.append("Add ");
        for (int i = 0; i < manaValues.size(); i++) {
            if (i > 0) {
                sb.append(" ");
            }
            sb.append(manaValues.get(i).toString());
        }
        return sb.toString();
    }

    /**
     * Internal effect that handles actual mana production during resolution.
     */
    private class ComposedManaEffect extends ManaEffect {

        private ComposedManaEffect() {
            super();
        }

        private ComposedManaEffect(final ComposedManaEffect effect) {
            super(effect);
        }

        @Override
        public List<Mana> getNetMana(Game game, Ability source) {
            return ComposedManaAbility.this.getNetMana(game);
        }

        @Override
        public Set<ManaType> getProducableManaTypes(Game game, Ability source) {
            return ComposedManaAbility.this.getProducableManaTypes(game);
        }

        @Override
        public Mana produceMana(Game game, Ability source) {
            Mana result = new Mana();
            for (ManaValue mv : manaValues) {
                List<Mana> evaluated = mv.evaluate(game, source, this);
                if (evaluated != null) {
                    for (Mana m : evaluated) {
                        result.add(m);
                    }
                }
            }
            return result;
        }

        @Override
        public ManaEffect copy() {
            return new ComposedManaEffect(this);
        }

        @Override
        public boolean apply(Game game, Ability source) {
            if (!spendingConditions.isEmpty()) {
                return applyConditional(game, source);
            }
            return super.apply(game, source);
        }

        private boolean applyConditional(Game game, Ability source) {
            Player player = getPlayer(game, source);
            if (player == null) {
                return false;
            }
            
            if (game.inCheckPlayableState()) {
                if (source.isTriggeredAbility()) {
                    player.addAvailableTriggeredMana(getNetMana(game, source));
                }
                return true;
            }

            Mana manaToAdd = produceMana(game, source);
            if (manaToAdd.count() > 0) {
                // Attach conditions directly to Mana instead of using ConditionalMana
                manaToAdd.addConditions(spendingConditions);
                manaToAdd.setConditionText(buildConditionText());
                checkToFirePossibleEvents(manaToAdd, game, source);
                addManaToPool(player, manaToAdd, game, source);
            }
            return true;
        }

        @Override
        protected void addManaToPool(Player player, Mana manaToAdd, Game game, Ability source) {
            // Use hasConditions() instead of instanceof ConditionalMana
            if (manaToAdd.hasConditions()) {
                player.getManaPool().addMana(manaToAdd, game, source);
            } else {
                super.addManaToPool(player, manaToAdd, game, source);
            }
        }
    }
}
