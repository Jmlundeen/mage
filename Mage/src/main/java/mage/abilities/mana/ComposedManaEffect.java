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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ComposedManaEffect extends ManaEffect {
        private final List<ManaValue> manaValues = new ArrayList<>();
        private final List<Condition> spendingConditions = new ArrayList<>();

        private ComposedManaEffect(final ComposedManaEffect effect) {
            super(effect);
            this.manaValues.addAll(effect.manaValues);
            this.spendingConditions.addAll(effect.spendingConditions);
        }

        public ComposedManaEffect(List<ManaValue> manaValues, List<Condition> spendingConditions) {
            super();
            this.manaValues.addAll(manaValues);
            this.spendingConditions.addAll(spendingConditions);
        }

    @Override
        public List<Mana> getNetMana(Game game, Ability source) {
            List<Mana> netMana = new ArrayList<>();
            if (game == null) {
                return netMana;
            }
            for (ManaValue mv : manaValues) {
                List<Mana> evaluated = mv.evaluate(game, source, this, false);
                if (evaluated != null) {
                    for (Mana m : evaluated) {
                        // Attach conditions to each mana option for playable calculations
                        if (!spendingConditions.isEmpty()) {
                            m.addConditions(spendingConditions);
                            m.setConditionText(buildConditionText());
                        }
                        netMana.add(m);
                    }
                }
            }
            return netMana;
        }

        @Override
        public Set<ManaType> getProducableManaTypes(Game game, Ability source) {
            Set<ManaType> types = HashSet.newHashSet(5);
            for (ManaValue mv : manaValues) {
                types.addAll(mv.getProducibleTypes());
            }
            return types.isEmpty() ? super.getProducableManaTypes(game, source) : types;
        }

        @Override
        public Mana produceMana(Game game, Ability source) {
            Mana result = new Mana();
            for (ManaValue mv : manaValues) {
                List<Mana> evaluated = mv.evaluate(game, source, this, true);
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

        private String buildConditionText() {
            StringBuilder sb = new StringBuilder();
            for (Condition condition : spendingConditions) {
                if (!sb.isEmpty()) sb.append(", ");
                sb.append(condition.getManaText());
            }
            return sb.toString();
        }

        @Override
        protected void addManaToPool(Player player, Mana manaToAdd, Game game, Ability source) {
            if (manaToAdd.hasConditions()) {
                player.getManaPool().addMana(manaToAdd, game, source);
            } else {
                super.addManaToPool(player, manaToAdd, game, source);
            }
        }
    }