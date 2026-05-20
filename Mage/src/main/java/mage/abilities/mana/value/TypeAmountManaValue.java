package mage.abilities.mana.value;

import mage.Mana;
import mage.abilities.Ability;
import mage.abilities.effects.Effect;
import mage.abilities.mana.providers.ManaTypeAmountProvider;
import mage.choices.Choice;
import mage.constants.ManaType;
import mage.constants.Outcome;
import mage.game.Game;
import mage.players.Player;

import java.util.*;

/**
 * Mana value for effects where each possible mana type has its own associated amount.
 */
public class TypeAmountManaValue implements ManaValue {

    private final ManaTypeAmountProvider manaTypeAmountProvider;

    public TypeAmountManaValue(ManaTypeAmountProvider manaTypeAmountProvider) {
        this.manaTypeAmountProvider = manaTypeAmountProvider;
    }

    @Override
    public List<Mana> evaluate(Game game, Ability source, Effect manaEffect, boolean produceMana) {
        if (game == null || manaTypeAmountProvider == null) {
            return Collections.emptyList();
        }

        Map<ManaType, Integer> manaAmounts = getManaAmounts(game, source, manaEffect);
        if (manaAmounts.isEmpty()) {
            return Collections.emptyList();
        }

        if (produceMana) {
            Player player = getChoicePlayer(game, source, manaEffect);
            if (player == null) {
                return Collections.emptyList();
            }
            Choice choice = ManaType.getChoiceOfManaTypes(manaAmounts.keySet(), !manaAmounts.containsKey(ManaType.COLORLESS));
            if (choice.getChoices().size() == 1) {
                choice.setChoice(choice.getChoices().iterator().next());
            } else if (!player.choose(Outcome.PutManaInPool, choice, game)) {
                return Collections.emptyList();
            }
            ManaType chosenType = ManaType.findByName(choice.getChoice());
            Integer amount = chosenType == null ? null : manaAmounts.get(chosenType);
            return chosenType == null || amount == null || amount <= 0
                    ? Collections.emptyList()
                    : Collections.singletonList(new Mana(chosenType, amount));
        }

        List<Mana> options = new ArrayList<>();
        for (Map.Entry<ManaType, Integer> entry : manaAmounts.entrySet()) {
            options.add(new Mana(entry.getKey(), entry.getValue()));
        }
        return options;
    }

    private Map<ManaType, Integer> getManaAmounts(Game game, Ability source, Effect manaEffect) {
        Map<ManaType, Integer> amounts = manaTypeAmountProvider.getManaAmounts(game, source, manaEffect);
        if (amounts == null || amounts.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<ManaType, Integer> result = new EnumMap<>(ManaType.class);
        for (Map.Entry<ManaType, Integer> entry : amounts.entrySet()) {
            if (entry.getKey() != null && entry.getKey() != ManaType.GENERIC && entry.getValue() != null && entry.getValue() > 0) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }

    @Override
    public Set<ManaType> getProducibleTypes() {
        return EnumSet.noneOf(ManaType.class);
    }

    @Override
    public Set<ManaType> getProducibleTypes(Game game, Ability source, Effect manaEffect) {
        Map<ManaType, Integer> manaAmounts = getManaAmounts(game, source, manaEffect);
        return manaAmounts.isEmpty() ? EnumSet.noneOf(ManaType.class) : EnumSet.copyOf(manaAmounts.keySet());
    }

    @Override
    public TypeAmountManaValue copy() {
        return new TypeAmountManaValue(manaTypeAmountProvider.copy());
    }
}
