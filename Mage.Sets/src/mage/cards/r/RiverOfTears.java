
package mage.cards.r;

import mage.Mana;
import mage.abilities.condition.common.PlayLandCondition;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.abilities.mana.value.ConditionalManaValue;
import mage.abilities.mana.value.StaticManaValue;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.watchers.common.PlayLandWatcher;

import java.util.UUID;

/**
 *
 * @author LevelX2
 */
public final class RiverOfTears extends CardImpl {

    public RiverOfTears(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.LAND}, "");

        // {T}: Add {U}. If you played a land this turn, add {B} instead.
        this.addAbility(ComposedManaAbilityBuilder.builder()
                .cost(new TapSourceCost())
                .addManaValue(new ConditionalManaValue(
                        new StaticManaValue(Mana.BlackMana(1)),
                        new StaticManaValue(Mana.BlueMana(1)),
                        PlayLandCondition.instance
                ))
                .ruleText("Add {U}. If you played a land this turn, add {B} instead")
                .build(),
                new PlayLandWatcher()
        );
    }

    private RiverOfTears(final RiverOfTears card) {
        super(card);
    }

    @Override
    public RiverOfTears copy() {
        return new RiverOfTears(this);
    }
}
