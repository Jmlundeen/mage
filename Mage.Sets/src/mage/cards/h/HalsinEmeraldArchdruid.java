package mage.cards.h;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.ChooseABackgroundAbility;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.costs.mana.GenericManaCost;
import mage.abilities.effects.common.continuous.BecomesCreatureTargetEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.FilterPermanent;
import mage.filter.common.FilterControlledPermanent;
import mage.filter.predicate.permanent.TokenPredicate;
import mage.game.permanent.token.custom.CreatureToken;
import mage.target.TargetPermanent;

import java.util.UUID;

/**
 * @author TheElk801
 */
public final class HalsinEmeraldArchdruid extends CardImpl {

    private static final FilterPermanent filter = new FilterControlledPermanent("token you control");

    static {
        filter.add(TokenPredicate.TRUE);
    }

    public HalsinEmeraldArchdruid(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{3}{G}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.ELF);
        this.subtype.add(SubType.DRUID);
        this.power = new MageInt(2);
        this.toughness = new MageInt(4);

        // {1}: Until end of turn, target token you control becomes a green Bear creature with base power and toughness 4/4 in addition to its other colors and types.
        Ability ability = new SimpleActivatedAbility(new BecomesCreatureTargetEffect(
                new CreatureToken(4, 4, "green Bear creature with base power and toughness 4/4 in addition to its other colors and types")
                        .withColor("G")
                        .withSubType(SubType.BEAR),
                false, false, Duration.EndOfTurn
        ).withDurationRuleAtStart(true), new GenericManaCost(1));
        ability.addTarget(new TargetPermanent(filter));
        this.addAbility(ability);

        // Choose a Background
        this.addAbility(ChooseABackgroundAbility.getInstance());
    }

    private HalsinEmeraldArchdruid(final HalsinEmeraldArchdruid card) {
        super(card);
    }

    @Override
    public HalsinEmeraldArchdruid copy() {
        return new HalsinEmeraldArchdruid(this);
    }
}
