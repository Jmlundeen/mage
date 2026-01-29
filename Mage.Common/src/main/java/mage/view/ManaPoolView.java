package mage.view;

import mage.ConditionalMana;
import mage.players.ManaPool;
import mage.ws.v1.view.ViewProto;

import java.io.Serializable;

/**
 *
 * @author BetaSteward_at_googlemail.com
 */
public class ManaPoolView implements Serializable {

    private static final long serialVersionUID = 1L;

    private int red;
    private int green;
    private int blue;
    private int white;
    private int black;
    private int colorless;

    public ManaPoolView(int red, int green, int blue, int white, int black, int colorless) {
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.white = white;
        this.black = black;
        this.colorless = colorless;
    }

    public ManaPoolView(ManaPool pool) {
        this.red = pool.getRed();
        this.green = pool.getGreen();
        this.blue = pool.getBlue();
        this.white = pool.getWhite();
        this.black = pool.getBlack();
        this.colorless = pool.getColorless();
        for (ConditionalMana mana : pool.getConditionalMana()) {
            this.red += mana.getRed();
            this.green += mana.getGreen();
            this.blue += mana.getBlue();
            this.white += mana.getWhite();
            this.black += mana.getBlack();
            this.colorless += mana.getColorless();
        }
    }

    public int getRed() {
        return red;
    }

    public int getGreen() {
        return green;
    }

    public int getBlue() {
        return blue;
    }

    public int getWhite() {
        return white;
    }

    public int getBlack() {
        return black;
    }

    public int getColorless() {
        return colorless;
    }

    public ViewProto.ManaPoolView toProto() {
        return ViewProto.ManaPoolView.newBuilder()
                .setRed(red)
                .setGreen(green)
                .setBlue(blue)
                .setWhite(white)
                .setBlack(black)
                .setColorless(colorless)
                .build();
    }

    public static ManaPoolView fromProto(ViewProto.ManaPoolView proto) {
        return new ManaPoolView(
                proto.getRed(),
                proto.getGreen(),
                proto.getBlue(),
                proto.getWhite(),
                proto.getBlack(),
                proto.getColorless()
        );
    }

}
