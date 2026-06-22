
package mage.cards.c;

import mage.abilities.common.EntersBattlefieldTappedAbility;
import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.mana.ColorlessManaAbility;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;

import java.util.UUID;

/**
 *
 * @author fireshoes
 */
public final class CrumblingVestige extends CardImpl {

    public CrumblingVestige(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.LAND}, "");

        // Crumbling Vestige enters the battlefield tapped.
        this.addAbility(new EntersBattlefieldTappedAbility());

        // When Crumbling Vestige enters the battlefield, add one mana of any color.
        this.addAbility(new EntersBattlefieldTriggeredAbility(
                ComposedManaAbilityBuilder.builder()
                    .addAnyColor(1)
                    .ruleText("add one mana of any color")
                    .buildEffect()
                ,false)
        );

        // {T}: Add {C} to you mana pool.
        this.addAbility(new ColorlessManaAbility());
    }

    private CrumblingVestige(final CrumblingVestige card) {
        super(card);
    }

    @Override
    public CrumblingVestige copy() {
        return new CrumblingVestige(this);
    }
}
