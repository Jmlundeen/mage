package mage.cards.c;

import mage.abilities.Ability;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.effects.common.continuous.BoostControlledEffect;
import mage.abilities.effects.common.continuous.generic.ContinuousEffectBuilder;
import mage.abilities.keyword.EmbalmAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.common.FilterCreatureCard;
import mage.filter.common.FilterCreaturePermanent;
import mage.target.common.TargetCardInYourGraveyard;

import java.util.UUID;

/**
 *
 * @author Jmlundeen
 */
public final class CurseclothWrappings extends CardImpl {
    public static final FilterCreaturePermanent filter = new FilterCreaturePermanent("Zombies you control");

    static {
        filter.add(SubType.ZOMBIE.getPredicate());
    }
    public CurseclothWrappings(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT}, "{2}{B}{B}");
        

        // Zombies you control get +1/+1.
        this.addAbility(new SimpleStaticAbility(new BoostControlledEffect(1, 1, Duration.WhileOnBattlefield, filter)));
        // {T}: Target creature card in your graveyard gains embalm until end of turn. The embalm cost is equal to its mana cost.
        Ability ability = new SimpleActivatedAbility(
                new ContinuousEffectBuilder(Duration.EndOfTurn, Outcome.AddAbility)
                        .withGainedAbility((card, source, game) -> new EmbalmAbility(card.getManaCost(), card))
                        .setText("Target creature card in your graveyard gains embalm until end of turn. " +
                                "The embalm cost is equal to its mana cost. (Exile that card and pay its embalm cost: " +
                                "Create a token that's a copy of it, except it's a white Zombie in addition to its other types " +
                                "and has no mana cost. Embalm only as a sorcery.)"),
                new TapSourceCost()
        );
        ability.addTarget(new TargetCardInYourGraveyard(new FilterCreatureCard("creature card in your graveyard")));
        this.addAbility(ability);
    }

    private CurseclothWrappings(final CurseclothWrappings card) {
        super(card);
    }

    @Override
    public CurseclothWrappings copy() {
        return new CurseclothWrappings(this);
    }
}
