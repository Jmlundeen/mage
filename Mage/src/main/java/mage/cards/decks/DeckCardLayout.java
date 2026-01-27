package mage.cards.decks;

import mage.util.Copyable;
import mage.ws.v1.model.ModelProto;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by stravant@gmail.com on 2016-10-03.
 */
public class DeckCardLayout implements Copyable<DeckCardLayout> {

    private final List<List<List<DeckCardInfo>>> cards;
    private final String settings;

    protected DeckCardLayout(final DeckCardLayout layout) {
        this.cards = new ArrayList<>();
        for (int i1 = 0; i1 < layout.cards.size(); i1++) {
            List<List<DeckCardInfo>> list1 = new ArrayList<>();
            this.cards.add(list1);
            for (int i2 = 0; i2 < layout.cards.get(i1).size(); i2++) {
                List<DeckCardInfo> list2 = new ArrayList<>();
                list1.add(list2);
                for (int i3 = 0; i3 < layout.cards.get(i1).get(i2).size(); i3++) {
                    DeckCardInfo info = layout.cards.get(i1).get(i2).get(i3);
                    list2.add(info.copy());
                }
            }
        }
        this.settings = layout.settings;
    }

    public DeckCardLayout(List<List<List<DeckCardInfo>>> cards, String settings) {
        this.cards = cards;
        this.settings = settings;
    }

    public List<List<List<DeckCardInfo>>> getCards() {
        return cards;
    }

    public String getSettings() {
        return settings;
    }

    @Override
    public DeckCardLayout copy() {
        return new DeckCardLayout(this);
    }

    public ModelProto.DeckCardLayout toProto() {
        ModelProto.DeckCardLayout.Builder builder = ModelProto.DeckCardLayout.newBuilder()
                .setSettings(this.settings != null ? this.settings : "");

        // Convert the nested List<List<List<DeckCardInfo>>> structure
        for (List<List<DeckCardInfo>> layer : this.cards) {
            ModelProto.CardLayoutLayer.Builder layerBuilder = ModelProto.CardLayoutLayer.newBuilder();
            for (List<DeckCardInfo> group : layer) {
                ModelProto.CardLayoutGroup.Builder groupBuilder = ModelProto.CardLayoutGroup.newBuilder();
                for (DeckCardInfo card : group) {
                    groupBuilder.addCards(card.toProto());
                }
                layerBuilder.addGroups(groupBuilder.build());
            }
            builder.addLayers(layerBuilder.build());
        }

        return builder.build();
    }

    public static DeckCardLayout fromProto(ModelProto.DeckCardLayout proto) {
        List<List<List<DeckCardInfo>>> cards = new ArrayList<>();

        // Convert the protobuf structure back to nested lists
        for (ModelProto.CardLayoutLayer layer : proto.getLayersList()) {
            List<List<DeckCardInfo>> layerList = new ArrayList<>();
            for (ModelProto.CardLayoutGroup group : layer.getGroupsList()) {
                List<DeckCardInfo> groupList = new ArrayList<>();
                for (ModelProto.DeckCardInfo cardProto : group.getCardsList()) {
                    groupList.add(DeckCardInfo.fromProto(cardProto));
                }
                layerList.add(groupList);
            }
            cards.add(layerList);
        }

        String settings = proto.getSettings().isEmpty() ? null : proto.getSettings();
        return new DeckCardLayout(cards, settings);
    }
}
