package mage.cards.i;

import mage.MageInt;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.abilities.mana.conditional.SpendOrActivateManaCondition;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.filter.FilterTyped;
import mage.filter.predicate.typed.mageObject.IMageObjectPredicate;

import java.util.UUID;

/**
 * @author Susucr
 */
public final class IxallisLorekeeper extends CardImpl {

    static final FilterTyped filter = new FilterTyped("dinosaur")
            .add(IMageObjectPredicate.getOSPPredicate(SubType.DINOSAUR.getPredicate()));

    public IxallisLorekeeper(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{G}");

        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.DRUID);
        this.power = new MageInt(1);
        this.toughness = new MageInt(1);

        // {T}: Add one mana of any color. Spend this mana only to cast a Dinosaur spell or activate an ability of a Dinosaur source.
        this.addAbility(new ComposedManaAbilityBuilder()
                .cost(new TapSourceCost())
                .addAnyColor(1)
                .condition(new SpendOrActivateManaCondition(filter))
                .ruleText("Add one mana of any color. Spend this mana only to cast a Dinosaur spell or activate an ability of a Dinosaur source")
                .build()
        );
    }

    private IxallisLorekeeper(final IxallisLorekeeper card) {
        super(card);
    }

    @Override
    public IxallisLorekeeper copy() {
        return new IxallisLorekeeper(this);
    }
}
