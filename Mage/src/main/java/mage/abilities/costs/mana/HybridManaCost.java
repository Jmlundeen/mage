package mage.abilities.costs.mana;

import mage.Mana;
import mage.abilities.Ability;
import mage.abilities.costs.Cost;
import mage.abilities.mana.ManaSourceNode;
import mage.constants.ColoredManaSymbol;
import mage.game.Game;
import mage.players.ManaPool;

import java.util.ArrayList;
import java.util.List;

public class HybridManaCost extends ManaCostImpl {

    private final ColoredManaSymbol mana1;
    private final ColoredManaSymbol mana2;

    public HybridManaCost(ColoredManaSymbol mana1, ColoredManaSymbol mana2) {
        this.mana1 = mana1;
        this.mana2 = mana2;
        this.cost = mana1 == null ? Mana.ColorlessMana(1) : new Mana(mana1);
        this.cost.add(new Mana(mana2));
        addColoredOption(mana1);
        addColoredOption(mana2);
    }

    public HybridManaCost(HybridManaCost manaCost) {
        super(manaCost);
        this.mana1 = manaCost.mana1;
        this.mana2 = manaCost.mana2;
    }

    @Override
    public int manaValue() {
        return 1;
    }

    @Override
    public boolean isPaid() {
        return paid || isColoredPaid(this.mana1) || isColoredPaid(this.mana2);
    }

    @Override
    public void assignPayment(Game game, Ability ability, ManaPool pool, Cost costToPay) {
        if (this.mana1 != null && assignColored(ability, game, pool, this.mana1, costToPay)) {
            return;
        }
        assignColored(ability, game, pool, this.mana2, costToPay);
    }

    @Override
    public String getText() {
        return '{' + mana1.toString() + '/' + mana2.toString() + (this.phyrexian ? "/P" : "") + '}';
    }

    @Override
    public HybridManaCost getUnpaid() {
        return this;
    }

    @Override
    public boolean testPay(ManaSourceNode testSource) {
        switch (mana1) {
            case B:
                if (testSource.getBlack() > 0 || testSource.getAny() > 0) {
                    return true;
                }
            case U:
                if (testSource.getBlue() > 0 || testSource.getAny() > 0) {
                    return true;
                }
            case R:
                if (testSource.getRed() > 0 || testSource.getAny() > 0) {
                    return true;
                }
            case W:
                if (testSource.getWhite() > 0 || testSource.getAny() > 0) {
                    return true;
                }
            case G:
                if (testSource.getGreen() > 0 || testSource.getAny() > 0) {
                    return true;
                }
            default:
                if (testSource.getColorless() > 0) {
                    return true;
                }
        }
        return switch (mana2) {
            case B -> testSource.getBlack() > 0 || testSource.getAny() > 0;
            case U -> testSource.getBlue() > 0 || testSource.getAny() > 0;
            case R -> testSource.getRed() > 0 || testSource.getAny() > 0;
            case W -> testSource.getWhite() > 0 || testSource.getAny() > 0;
            case G -> testSource.getGreen() > 0 || testSource.getAny() > 0;
            default -> false;
        };
    }

    @Override
    public HybridManaCost copy() {
        return new HybridManaCost(this);
    }

    @Override
    public boolean containsColor(ColoredManaSymbol coloredManaSymbol) {
        return mana1 == coloredManaSymbol || mana2 == coloredManaSymbol;
    }

    @Override
    public List<Mana> getManaOptions() {
        List<Mana> manaList = new ArrayList<>();
        manaList.add(new Mana(mana1));
        manaList.add(new Mana(mana2));
        return manaList;
    }

    public ColoredManaSymbol getMana1() {
        return mana1;
    }

    public ColoredManaSymbol getMana2() {
        return mana2;
    }
}
