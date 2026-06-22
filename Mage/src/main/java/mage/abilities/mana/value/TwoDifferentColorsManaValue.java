package mage.abilities.mana.value;

import mage.Mana;
import mage.abilities.Ability;
import mage.abilities.effects.Effect;
import mage.choices.ManaChoice;
import mage.constants.ManaType;
import mage.game.Game;
import mage.players.Player;

import java.util.*;

public class TwoDifferentColorsManaValue implements ManaValue {

    private static final List<ManaType> colorsToCycle = new ArrayList<>();
    private static final List<Mana> netMana = new ArrayList<>();

    static {
        colorsToCycle.add(ManaType.WHITE);
        colorsToCycle.add(ManaType.BLUE);
        colorsToCycle.add(ManaType.BLACK);
        colorsToCycle.add(ManaType.RED);
        colorsToCycle.add(ManaType.GREEN);

        for (ManaType type1 : colorsToCycle) {
            for (ManaType type2 : colorsToCycle) {
                if (type1 == type2) {
                    continue;
                }
                Mana combo = new Mana(type1);
                combo.increase(type2);
                netMana.add(combo);
            }
        }
    }

    @Override
    public List<Mana> evaluate(Game game, Ability source, Effect manaEffect, boolean produceMana) {
        if (produceMana && game != null) {
            Player player = getChoicePlayer(game, source, manaEffect);
            if (player == null) {
                return Collections.emptyList();
            }
            Mana mana = ManaChoice.chooseTwoDifferentColors(player, game);
            return Collections.singletonList(mana);
        }
        return netMana;
    }

    @Override
    public Set<ManaType> getProducibleTypes() {
        return EnumSet.of(ManaType.WHITE, ManaType.BLUE, ManaType.BLACK, ManaType.RED, ManaType.GREEN);
    }

    @Override
    public ManaValue copy() {
        return new TwoDifferentColorsManaValue();
    }
}
