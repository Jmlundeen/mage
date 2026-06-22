package mage.cards.t;

import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.effects.keyword.ScryEffect;
import mage.abilities.mana.AnyColorAmongManaAbility;
import mage.abilities.mana.ColorlessManaAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SuperType;
import mage.constants.Zone;
import mage.filter.FilterTyped;
import mage.filter.predicate.typed.card.ICardPredicate;

import java.util.UUID;

/**
 *
 * @author Susucr
 */
public final class TheGreyHavens extends CardImpl {

    private static final FilterTyped filter = new FilterTyped("legendary creature cards in your graveyard")
            .add((ICardPredicate) (osp, game) ->
                    osp.getObject().getSuperType(game).contains(SuperType.LEGENDARY) && osp.getObject().getCardType(game).contains(CardType.CREATURE));

    public TheGreyHavens(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.LAND}, "");
        
        this.supertype.add(SuperType.LEGENDARY);

        // When The Grey Havens enters the battlefield, scry 1.
        this.addAbility(new EntersBattlefieldTriggeredAbility(new ScryEffect(1, false)));

        // {T}: Add {C}.
        this.addAbility(new ColorlessManaAbility());

        // {T}: Add one mana of any color among legendary creature cards in your graveyard.
        this.addAbility(AnyColorAmongManaAbility.builder(filter, Zone.GRAVEYARD)
                .onlyColors(true)
                .ruleText("Add one mana of any color among legendary creature cards in your graveyard")
                .build()
        );
    }

    private TheGreyHavens(final TheGreyHavens card) {
        super(card);
    }

    @Override
    public TheGreyHavens copy() {
        return new TheGreyHavens(this);
    }
}
