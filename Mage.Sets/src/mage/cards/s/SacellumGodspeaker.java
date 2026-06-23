package mage.cards.s;

import mage.MageInt;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.dynamicvalue.DynamicValue;
import mage.abilities.dynamicvalue.common.RevealedCardsAmount;
import mage.abilities.effects.common.reveal.RevealEffect;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.abilities.mana.SimpleManaAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.common.FilterCreatureCard;
import mage.filter.predicate.mageobject.PowerPredicate;

import java.util.UUID;

/**
 * @author Plopman
 */
public final class SacellumGodspeaker extends CardImpl {

    private static final FilterCreatureCard filter = new FilterCreatureCard("creature cards with power 5 or greater from your hand");
    private static final DynamicValue revealedCardsValue = new RevealedCardsAmount(filter);

    static {
        filter.add(new PowerPredicate(ComparisonType.MORE_THAN, 4));
    }

    public SacellumGodspeaker(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{2}{G}");
        this.subtype.add(SubType.ELF);
        this.subtype.add(SubType.DRUID);

        this.power = new MageInt(2);
        this.toughness = new MageInt(2);

        // {T}: Reveal any number of creature cards with power 5 or greater from your hand. Add {G} for each card revealed this way.
        SimpleManaAbility ability = new SimpleManaAbility(new RevealEffect(Outcome.Benefit)
                .setFilter(filter)
                .setMinCardsToReveal(0)
                .rememberRevealed()
                .setText("Reveal any number of creature cards with power 5 or greater from your hand"), new TapSourceCost());
        ability.addEffect(new ComposedManaAbilityBuilder()
                .addDynamic(revealedCardsValue, ManaType.GREEN)
                .ruleText("Add {G} for each card revealed this way")
                .buildEffect()
        );
        ability.setUndoPossible(false);
        this.addAbility(ability);
    }

    private SacellumGodspeaker(final SacellumGodspeaker card) {
        super(card);
    }

    @Override
    public SacellumGodspeaker copy() {
        return new SacellumGodspeaker(this);
    }
}
