package mage.cards.m;

import mage.abilities.mana.AnyColorAmongManaAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SuperType;
import mage.constants.TargetController;
import mage.filter.FilterTyped;
import mage.filter.predicate.typed.LogicalPredicate;

import java.util.UUID;

/**
 * @author CountAndromalius
 */
public final class MoxAmber extends CardImpl {

    private static final FilterTyped filter = new FilterTyped("legendary creatures and planeswalkers you control")
            .add(SuperType.LEGENDARY.getPredicate())
            .add(TargetController.YOU.getControllerPredicate())
            .add(LogicalPredicate.or(
                    CardType.CREATURE.getPredicate(),
                    CardType.PLANESWALKER.getPredicate()
            ));

    public MoxAmber(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT}, "{0}");
        this.supertype.add(SuperType.LEGENDARY);

        // {T}: Add one mana of any color among legendary creatures and planeswalkers you control.
        this.addAbility(AnyColorAmongManaAbility.builder(filter)
                .onlyColors(true)
                .ruleText("Add one mana of any color among legendary creatures and planeswalkers you control.")
                .build()
        );
    }

    private MoxAmber(final MoxAmber card) {
        super(card);
    }

    @Override
    public MoxAmber copy() {
        return new MoxAmber(this);
    }
}
