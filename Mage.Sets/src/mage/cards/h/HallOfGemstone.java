package mage.cards.h;

import mage.abilities.Ability;
import mage.abilities.effects.common.ChooseColorEffect;
import mage.abilities.effects.mana.ReplaceManaEffect;
import mage.abilities.triggers.BeginningOfUpkeepTriggeredAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.StaticTypedFilters;

import java.util.UUID;

/**
 * @author LevelX2
 */
public final class HallOfGemstone extends CardImpl {

    public HallOfGemstone(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ENCHANTMENT}, "{1}{G}{G}");

        this.supertype.add(SuperType.WORLD);

        // At the beginning of each player's upkeep, that player chooses a color. Until end of turn, lands tapped for mana produce mana of the chosen color instead of any other color.
        Ability ability = new BeginningOfUpkeepTriggeredAbility(TargetController.EACH_PLAYER, new ChooseColorEffect(Outcome.Neutral, TargetController.ACTIVE), false);
        ability.addEffect(ReplaceManaEffect.produced(Duration.EndOfTurn, Outcome.Neutral, ReplaceManaEffect.replaceColorWithChosenColor())
                .setProducedMatcher(StaticTypedFilters.A_LAND)
                .setText("Until end of turn, lands tapped for mana produce mana of the chosen color instead of any other color")
        );
        this.addAbility(ability);
    }

    private HallOfGemstone(final HallOfGemstone card) {
        super(card);
    }

    @Override
    public HallOfGemstone copy() {
        return new HallOfGemstone(this);
    }
}
