package mage.cards.f;

import mage.MageInt;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.condition.common.FerociousCondition;
import mage.abilities.decorator.ConditionalReplacementEffect;
import mage.abilities.effects.common.continuous.replacement.EntersWithCountersEffect;
import mage.abilities.hint.common.FerociousHint;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.counters.CounterType;

import java.util.UUID;

/**
 * @author LevelX2
 */
public final class FrontierMastodon extends CardImpl {

    public FrontierMastodon(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{2}{G}");
        this.subtype.add(SubType.ELEPHANT);
        this.power = new MageInt(3);
        this.toughness = new MageInt(2);

        // <i>Ferocious</i> &mdash; Frontier Mastodon enters the battlefield with a +1/+1 counter on it if you control a creature with power 4 or greater.
        this.addAbility(new SimpleStaticAbility(new ConditionalReplacementEffect(
                new EntersWithCountersEffect(CounterType.P1P1.createInstance(1)),
                FerociousCondition.instance)
                .setText("{this} enters with a +1/+1 counter on it if you control a creature with power 4 or greater."))
                .withFlavorWord("Ferocious")
                .addHint(FerociousHint.instance));
    }

    private FrontierMastodon(final FrontierMastodon card) {
        super(card);
    }

    @Override
    public FrontierMastodon copy() {
        return new FrontierMastodon(this);
    }
}
