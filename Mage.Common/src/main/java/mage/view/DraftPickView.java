

package mage.view;

import mage.game.draft.DraftPlayer;
import mage.ws.view.ViewProto;

import java.io.Serializable;

/**
 *
 * @author BetaSteward_at_googlemail.com
 */
public class DraftPickView implements Serializable {
    private static final long serialVersionUID = 1L;

    protected SimpleCardsView booster;
    protected SimpleCardsView picks;
    protected boolean picking;
    protected int timeout;

    public DraftPickView(DraftPlayer player, int timeout) {
        this.booster = new SimpleCardsView(player.getBooster(), false);
        this.picks = new SimpleCardsView(player.getDeck().getSideboard(), false);
        this.picking = player.isPicking();
        this.timeout = timeout;
    }

    public SimpleCardsView getBooster() {
        return booster;
    }

    public SimpleCardsView getPicks() {
        return picks;
    }

    public boolean isPicking() {
        return this.picking;
    }

    public int getTimeout() {
        return timeout;
    }

    public ViewProto.DraftPickView toProto() {
        return ViewProto.DraftPickView.newBuilder()
                .setBooster(booster != null ? booster.toProto() : ViewProto.SimpleCardsView.getDefaultInstance())
                .setPicks(picks != null ? picks.toProto() : ViewProto.SimpleCardsView.getDefaultInstance())
                .setPicking(picking)
                .setTimeout(timeout)
                .build();
    }

    public static DraftPickView fromProto(ViewProto.DraftPickView proto) {
        DraftPickView view = new DraftPickView();
        view.booster = proto.hasBooster() ? SimpleCardsView.fromProto(proto.getBooster()) : new SimpleCardsView();
        view.picks = proto.hasPicks() ? SimpleCardsView.fromProto(proto.getPicks()) : new SimpleCardsView();
        view.picking = proto.getPicking();
        view.timeout = proto.getTimeout();
        return view;
    }

    // Add default constructor for fromProto
    private DraftPickView() {
    }
}
