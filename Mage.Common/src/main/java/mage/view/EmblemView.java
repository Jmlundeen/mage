package mage.view;

import mage.game.Game;
import mage.game.command.Emblem;
import mage.game.command.emblems.EmblemOfCard;
import mage.players.PlayableObjectStats;
import mage.ws.v1.view.ViewProto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author noxx
 */
public class EmblemView implements CommandObjectView, Serializable {

    protected UUID id;
    protected String name;
    protected String cardNumber = "";
    protected String imageFileName = "";
    protected int imageNumber;
    protected boolean usesVariousArt = false;
    protected String expansionSetCode;
    protected List<String> rules;
    protected PlayableObjectStats playableStats = new PlayableObjectStats();

    public EmblemView(Emblem emblem, Game game) {
        this.id = emblem.getId();
        this.name = emblem.getName();
        this.imageFileName = emblem.getImageFileName();
        this.imageNumber = emblem.getImageNumber();
        this.expansionSetCode = emblem.getExpansionSetCode();
        this.rules = emblem.getAbilities().getRules(game, emblem);
        if (emblem instanceof EmblemOfCard) {
            cardNumber = emblem.getCardNumber();
            usesVariousArt = ((EmblemOfCard) emblem).getUsesVariousArt();
        }
    }

    @Override
    public String getExpansionSetCode() {
        return expansionSetCode;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public UUID getId() {
        return id;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    @Override
    public String getImageFileName() {
        return imageFileName;
    }

    @Override
    public int getImageNumber() {
        return imageNumber;
    }

    public boolean getUsesVariousArt() {
        return this.usesVariousArt;
    }

    @Override
    public List<String> getRules() {
        return rules;
    }

    public ViewProto.EmblemView toProto() {
        return ViewProto.EmblemView.newBuilder()
                .setId(id.toString())
                .setName(name != null ? name : "")
                .setCardNumber(cardNumber != null ? cardNumber : "")
                .setImageFileName(imageFileName != null ? imageFileName : "")
                .setImageNumber(imageNumber)
                .setUsesVariousArt(usesVariousArt)
                .setExpansionSetCode(expansionSetCode != null ? expansionSetCode : "")
                .addAllRules(rules != null ? rules : new ArrayList<>())
                .build();
    }

    public static EmblemView fromProto(ViewProto.EmblemView proto) {
        // Since original logic uses Emblem object in constructor, we need to manually fill
        EmblemView view = new EmblemView();
        view.id = UUID.fromString(proto.getId());
        view.name = proto.getName();
        view.cardNumber = proto.getCardNumber();
        view.imageFileName = proto.getImageFileName();
        view.imageNumber = proto.getImageNumber();
        view.usesVariousArt = proto.getUsesVariousArt();
        view.expansionSetCode = proto.getExpansionSetCode();
        view.rules = new ArrayList<>(proto.getRulesList());
        return view;
    }

    protected EmblemView() {
    }

    @Override
    public boolean isPlayable() {
        return this.playableStats.getPlayableAmount() > 0;
    }

    @Override
    public void setPlayableStats(PlayableObjectStats playableStats) {
        this.playableStats = playableStats;
    }

    @Override
    public PlayableObjectStats getPlayableStats() {
        return this.playableStats;
    }

    @Override
    public boolean isChoosable() {
        // unsupported
        return false;
    }

    @Override
    public void setChoosable(boolean isChoosable) {
        // unsupported
    }

    @Override
    public boolean isSelected() {
        // unsupported
        return false;
    }

    @Override
    public void setSelected(boolean isSelected) {
        // unsupported
    }
}
