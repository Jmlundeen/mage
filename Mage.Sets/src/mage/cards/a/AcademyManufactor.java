package mage.cards.a;

import mage.MageInt;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.common.replacement.ReplaceTokenEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.game.permanent.token.ClueArtifactToken;
import mage.game.permanent.token.FoodToken;
import mage.game.permanent.token.TreasureToken;

import java.util.UUID;

/**
 * @author weirddan455
 */
public final class AcademyManufactor extends CardImpl {

    public AcademyManufactor(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT, CardType.CREATURE}, "{3}");

        this.subtype.add(SubType.ASSEMBLY_WORKER);
        this.power = new MageInt(1);
        this.toughness = new MageInt(3);

        // If you would create a Clue, Food, or Treasure token, instead create one of each.
        this.addAbility(new SimpleStaticAbility(new ReplaceTokenEffect(ReplaceTokenEffect.ModificationType.REPLACE, 1, new ClueArtifactToken())
                .withTokenCondition((token, game) -> token.hasSubtype(SubType.CLUE, game)
                        || token.hasSubtype(SubType.FOOD, game)
                        || token.hasSubtype(SubType.TREASURE, game))
                .withAdditionalTokens(new FoodToken())
                .withAdditionalTokens(new TreasureToken())
                .setText("If you would create a Clue, Food, or Treasure token, instead create one of each")
        ));
    }

    private AcademyManufactor(final AcademyManufactor card) {
        super(card);
    }

    @Override
    public AcademyManufactor copy() {
        return new AcademyManufactor(this);
    }
}
