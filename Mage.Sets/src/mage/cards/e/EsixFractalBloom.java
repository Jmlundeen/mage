package mage.cards.e;

import mage.MageInt;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.condition.common.FirstTimeCreateTokensCondition;
import mage.abilities.decorator.ConditionalReplacementEffect;
import mage.abilities.effects.ReplacementEffect;
import mage.abilities.effects.common.replacement.ReplaceTokenEffect;
import mage.abilities.keyword.FlyingAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.filter.FilterPermanent;
import mage.filter.common.FilterCreaturePermanent;
import mage.filter.predicate.mageobject.AnotherPredicate;
import mage.target.TargetPermanent;
import mage.watchers.common.CreatedTokenWatcher;

import java.util.UUID;

/**
 * @author TheElk801
 */
public final class EsixFractalBloom extends CardImpl {

    private static final FilterPermanent filter = new FilterCreaturePermanent("another creature");

    static {
        filter.add(AnotherPredicate.instance);
    }

    public EsixFractalBloom(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{4}{G}{U}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.FRACTAL);
        this.power = new MageInt(4);
        this.toughness = new MageInt(4);

        // Flying
        this.addAbility(FlyingAbility.getInstance());

        // The first time you would create one or more tokens during each of your turns, you may instead choose a creature other than Esix, Fractal Bloom and create that many tokens that are copies of that creature.
        ReplacementEffect effect = new ReplaceTokenEffect(ReplaceTokenEffect.ModificationType.REPLACE_CHOOSE, 0, null)
                .withChosenPermanent(new TargetPermanent(0, 1, filter, true));
        effect.setText("the first time you would create one or more tokens during each of your turns, " +
                        "you may instead choose a creature other than {this} " +
                        "and create that many tokens that are copies of that creature");
        this.addAbility(new SimpleStaticAbility(new ConditionalReplacementEffect(
                effect, FirstTimeCreateTokensCondition.YOUR_TURN
        )), new CreatedTokenWatcher());
    }

    private EsixFractalBloom(final EsixFractalBloom card) {
        super(card);
    }

    @Override
    public EsixFractalBloom copy() {
        return new EsixFractalBloom(this);
    }
}
