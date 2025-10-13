package mage.cards.w;

import mage.MageObject;
import mage.abilities.Ability;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.common.CreateTokenCopyTargetEffect;
import mage.abilities.effects.common.replacement.ReplaceTokenEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.filter.FilterPermanent;
import mage.filter.common.FilterControlledArtifactPermanent;
import mage.filter.predicate.permanent.TokenPredicate;
import mage.game.permanent.token.MapToken;
import mage.target.TargetPermanent;

import java.util.UUID;

/**
 * @author TheElk801
 */
public final class WorldwalkerHelm extends CardImpl {

    private static final FilterPermanent filter = new FilterControlledArtifactPermanent("artifact token you control");

    static {
        filter.add(TokenPredicate.TRUE);
    }

    public WorldwalkerHelm(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT}, "{2}{U}");

        // If you would create one or more artifact tokens, instead create those tokens plus an additional Map token.
        this.addAbility(new SimpleStaticAbility(new ReplaceTokenEffect(ReplaceTokenEffect.ModificationType.ADD, 1, new MapToken())
                .withTokenCondition(MageObject::isArtifact)
                .setText("if you would create one or more artifact tokens, instead create those tokens plus an additional Map token")
        ));

        // {1}{U}, {T}: Create a token that's a copy of target artifact token you control.
        Ability ability = new SimpleActivatedAbility(new CreateTokenCopyTargetEffect(), new ManaCostsImpl<>("{1}{U}"));
        ability.addCost(new TapSourceCost());
        ability.addTarget(new TargetPermanent(filter));
        this.addAbility(ability);
    }

    private WorldwalkerHelm(final WorldwalkerHelm card) {
        super(card);
    }

    @Override
    public WorldwalkerHelm copy() {
        return new WorldwalkerHelm(this);
    }
}
