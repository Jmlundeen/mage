package mage.cards.h;

import mage.MageInt;
import mage.Mana;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.costs.common.DiscardCardCost;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.mana.ReplaceManaEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.filter.StaticTypedFilters;

import java.util.UUID;

/**
 * @author L_J
 */
public final class HarvestMage extends CardImpl {

    public HarvestMage(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{G}");
        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.SPELLSHAPER);
        this.power = new MageInt(1);
        this.toughness = new MageInt(1);

        // {G}, {T}, Discard a card: Until end of turn, if you tap a land for mana, it produces one mana of a color of your choice instead of any other type and amount.
        SimpleActivatedAbility ability = new SimpleActivatedAbility(
                ReplaceManaEffect.produced(Duration.EndOfTurn, Outcome.Neutral, ReplaceManaEffect.replaceAllProducedMana(Mana.AnyMana(1)))
                        .setProducedMatcher(StaticTypedFilters.LAND_YOU_CONTROL)
                        .setText("Until end of turn, if you tap a land for mana, it produces one mana of a color of your choice instead of any other type and amount"),
                new ManaCostsImpl<>("{G}")
        );
        ability.addCost(new TapSourceCost());
        ability.addCost(new DiscardCardCost());
        this.addAbility(ability);
    }

    private HarvestMage(final HarvestMage card) {
        super(card);
    }

    @Override
    public HarvestMage copy() {
        return new HarvestMage(this);
    }
}
