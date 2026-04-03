package mage.abilities.mana.value;

import mage.Mana;
import mage.abilities.Ability;
import mage.abilities.effects.Effect;
import mage.constants.ManaType;
import mage.game.Game;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * A static mana value that produces a fixed amount of mana.
 * This value does not depend on game state and can be cached.
 */
public class StaticManaValue implements ManaValue {

    private final int white;
    private final int blue;
    private final int black;
    private final int red;
    private final int green;
    private final int generic;
    private final int colorless;
    private final int any;

    public StaticManaValue(int white, int blue, int black, int red, int green, int generic, int colorless, int any) {
        this.white = white;
        this.blue = blue;
        this.black = black;
        this.red = red;
        this.green = green;
        this.generic = generic;
        this.colorless = colorless;
        this.any = any;
    }

    public StaticManaValue(Mana mana) {
        this.white = mana.getWhite();
        this.blue = mana.getBlue();
        this.black = mana.getBlack();
        this.red = mana.getRed();
        this.green = mana.getGreen();
        this.generic = mana.getGeneric();
        this.colorless = mana.getColorless();
        this.any = mana.getAny();
    }

    @Override
    public List<Mana> evaluate(Game game, Ability source, Effect manaEffect) {
        return Collections.singletonList(new Mana(white, blue, black, red, green, generic, any, colorless));
    }

    @Override
    public Set<ManaType> getProducibleTypes() {
        Set<ManaType> types = EnumSet.noneOf(ManaType.class);
        if (white > 0) types.add(ManaType.WHITE);
        if (blue > 0) types.add(ManaType.BLUE);
        if (black > 0) types.add(ManaType.BLACK);
        if (red > 0) types.add(ManaType.RED);
        if (green > 0) types.add(ManaType.GREEN);
        if (generic > 0 || colorless > 0) types.add(ManaType.COLORLESS);
        if (any > 0) {
            types.add(ManaType.WHITE);
            types.add(ManaType.BLUE);
            types.add(ManaType.BLACK);
            types.add(ManaType.RED);
            types.add(ManaType.GREEN);
        }
        return types;
    }

    @Override
    public boolean isStatic() {
        return true;
    }

    public int getWhite() { return white; }
    public int getBlue() { return blue; }
    public int getBlack() { return black; }
    public int getRed() { return red; }
    public int getGreen() { return green; }
    public int getGeneric() { return generic; }
    public int getColorless() { return colorless; }
    public int getAny() { return any; }

    public Mana toMana() {
        return new Mana(white, blue, black, red, green, generic, any, colorless);
    }

    @Override
    public StaticManaValue copy() {
        return new StaticManaValue(white, blue, black, red, green, generic, colorless, any);
    }

    @Override
    public String toString() {
        return toMana().toString();
    }

    public static StaticManaValue white(int amount) {
        return new StaticManaValue(amount, 0, 0, 0, 0, 0, 0, 0);
    }

    public static StaticManaValue blue(int amount) {
        return new StaticManaValue(0, amount, 0, 0, 0, 0, 0, 0);
    }

    public static StaticManaValue black(int amount) {
        return new StaticManaValue(0, 0, amount, 0, 0, 0, 0, 0);
    }

    public static StaticManaValue red(int amount) {
        return new StaticManaValue(0, 0, 0, amount, 0, 0, 0, 0);
    }

    public static StaticManaValue green(int amount) {
        return new StaticManaValue(0, 0, 0, 0, amount, 0, 0, 0);
    }

    public static StaticManaValue colorless(int amount) {
        return new StaticManaValue(0, 0, 0, 0, 0, 0, amount, 0);
    }

    public static StaticManaValue generic(int amount) {
        return new StaticManaValue(0, 0, 0, 0, 0, amount, 0, 0);
    }

    public static StaticManaValue any(int amount) {
        return new StaticManaValue(0, 0, 0, 0, 0, 0, 0, amount);
    }
}
