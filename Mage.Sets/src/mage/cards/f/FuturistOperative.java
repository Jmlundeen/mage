package mage.cards.f;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.condition.common.SourceTappedCondition;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.decorator.ConditionalContinuousEffect;
import mage.abilities.decorator.ConditionalRestrictionEffect;
import mage.abilities.effects.common.UntapSourceEffect;
import mage.abilities.effects.common.combat.CantBeBlockedSourceEffect;
import mage.abilities.effects.common.continuous.BecomesCreatureSourceEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.game.permanent.token.custom.CreatureToken;

import java.util.UUID;

/**
 * @author TheElk801
 */
public final class FuturistOperative extends CardImpl {

    public FuturistOperative(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{3}{U}");

        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.NINJA);
        this.power = new MageInt(3);
        this.toughness = new MageInt(4);

        // As long as Futurist Operative is tapped, it's a Human Citizen with base power and toughness 1/1 and can't be blocked.
        Ability ability = new SimpleStaticAbility(new ConditionalContinuousEffect(
                new BecomesCreatureSourceEffect(new CreatureToken(1, 1, "Human Citizen with base power and toughness 1/1")
                        .withSubType(SubType.HUMAN)
                        .withSubType(SubType.CITIZEN), null, Duration.WhileOnBattlefield),
                SourceTappedCondition.TAPPED,
                "as long as {this} is tapped, it's a Human Citizen with base power and toughness 1/1")
        );
        ability.addEffect(new ConditionalRestrictionEffect(
                new CantBeBlockedSourceEffect(), SourceTappedCondition.TAPPED, "and can't be blocked"
        ));
        this.addAbility(ability);

        // {2}{U}: Untap Futurist Operative.
        this.addAbility(new SimpleActivatedAbility(new UntapSourceEffect(), new ManaCostsImpl<>("{2}{U}")));
    }

    private FuturistOperative(final FuturistOperative card) {
        super(card);
    }

    @Override
    public FuturistOperative copy() {
        return new FuturistOperative(this);
    }
}
