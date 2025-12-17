package mage.cards.o;

import mage.abilities.Ability;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.condition.common.PermanentsOnTheBattlefieldCondition;
import mage.abilities.costs.common.SacrificeTargetCost;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.decorator.ConditionalOneShotEffect;
import mage.abilities.dynamicvalue.common.PermanentsOnBattlefieldCount;
import mage.abilities.effects.common.CreateTokenEffect;
import mage.abilities.effects.common.FlipSourceEffect;
import mage.abilities.effects.common.continuous.BoostTargetEffect;
import mage.abilities.hint.ValueHint;
import mage.cards.CardSetInfo;
import mage.cards.FlipCard;
import mage.constants.*;
import mage.filter.StaticFilters;
import mage.filter.common.FilterControlledCreaturePermanent;
import mage.game.permanent.token.SnakeToken;
import mage.target.common.TargetCreaturePermanent;

import java.util.UUID;

/**
 * @author LevelX
 */
public final class OrochiEggwatcher extends FlipCard {

    public OrochiEggwatcher(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo,
                new CardType[]{CardType.CREATURE}, new SubType[]{SubType.SNAKE, SubType.SHAMAN}, "{2}{G}",
                "Shidako, Broodmistress",
                new SuperType[]{SuperType.LEGENDARY}, new CardType[]{CardType.CREATURE}, new SubType[]{SubType.SNAKE, SubType.SHAMAN});

        // Orochi Eggwatcher
        this.getLeftHalfCard().setPT(1, 1);

        // {2}{G}, {T}: Create a 1/1 green Snake creature token. If you control ten or more creatures, flip Orochi Eggwatcher.
        Ability ability;
        ability = new SimpleActivatedAbility(new CreateTokenEffect(new SnakeToken()), new ManaCostsImpl<>("{2}{G}"));
        ability.addCost(new TapSourceCost());
        ability.addEffect(new ConditionalOneShotEffect(new FlipSourceEffect(),
                new PermanentsOnTheBattlefieldCondition(new FilterControlledCreaturePermanent(), ComparisonType.MORE_THAN, 9), "If you control ten or more creatures, flip {this}"));
        this.getLeftHalfCard().addAbility(ability.addHint(new ValueHint("Creatures you control", new PermanentsOnBattlefieldCount(StaticFilters.FILTER_CONTROLLED_CREATURE))));

        // Shidako, Broodmistress
        this.getRightHalfCard().setPT(3, 3);

        // {G}, Sacrifice a creature: Target creature gets +3/+3 until end of turn.
        Ability ability2 = new SimpleActivatedAbility(
                new BoostTargetEffect(3, 3, Duration.EndOfTurn),
                new ManaCostsImpl<>("{G}"));
        ability2.addCost(new SacrificeTargetCost(StaticFilters.FILTER_PERMANENT_CREATURE));
        ability2.addTarget(new TargetCreaturePermanent());
        this.getRightHalfCard().addAbility(ability2);
    }

    private OrochiEggwatcher(final OrochiEggwatcher card) {
        super(card);
    }

    @Override
    public OrochiEggwatcher copy() {
        return new OrochiEggwatcher(this);
    }
}
