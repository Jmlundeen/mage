package mage.cards.c;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.costs.common.SacrificeXTargetCost;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.dynamicvalue.common.GetXValue;
import mage.abilities.dynamicvalue.common.SignInversionDynamicValue;
import mage.abilities.effects.common.continuous.BoostTargetEffect;
import mage.abilities.effects.common.replacement.ReplaceTokenEffect;
import mage.abilities.keyword.ForestwalkAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.filter.common.FilterControlledPermanent;
import mage.game.permanent.token.SquirrelToken;
import mage.target.common.TargetCreaturePermanent;

import java.util.UUID;

/**
 * @author weirddan455
 */
public final class ChatterfangSquirrelGeneral extends CardImpl {

    private static final FilterControlledPermanent filter = new FilterControlledPermanent(SubType.SQUIRREL, "Squirrels");

    public ChatterfangSquirrelGeneral(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{2}{G}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.SQUIRREL);
        this.subtype.add(SubType.WARRIOR);
        this.power = new MageInt(3);
        this.toughness = new MageInt(3);

        // Forestwalk
        this.addAbility(new ForestwalkAbility());

        // If one or more tokens would be created under your control, those tokens plus that many 1/1 green Squirrel creature tokens are created instead.
        this.addAbility(new SimpleStaticAbility(new ReplaceTokenEffect(ReplaceTokenEffect.ModificationType.THAT_MANY, 0,
                new SquirrelToken())
                .setText("If one or more tokens would be created under your control, " +
                        "those tokens plus that many 1/1 green Squirrel creature tokens are created instead.")
        ));

        // {B}, Sacrifice X Squirrels: Target creature gets +X/-X until end of turn.
        Ability ability = new SimpleActivatedAbility(new BoostTargetEffect(
                GetXValue.instance, new SignInversionDynamicValue(GetXValue.instance), Duration.EndOfTurn
        ), new ManaCostsImpl<>("{B}"));
        ability.addCost(new SacrificeXTargetCost(filter));
        ability.addTarget(new TargetCreaturePermanent());
        this.addAbility(ability);
    }

    private ChatterfangSquirrelGeneral(final ChatterfangSquirrelGeneral card) {
        super(card);
    }

    @Override
    public ChatterfangSquirrelGeneral copy() {
        return new ChatterfangSquirrelGeneral(this);
    }
}
