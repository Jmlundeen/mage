package mage.cards.s;

import mage.MageInt;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.condition.common.RevealedOrControlledDragonCondition;
import mage.abilities.costs.common.RevealDragonFromHandCost;
import mage.abilities.decorator.ConditionalReplacementEffect;
import mage.abilities.effects.common.continuous.replacement.EntersWithCountersEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.counters.CounterType;
import mage.watchers.common.DragonOnTheBattlefieldWhileSpellWasCastWatcher;

import java.util.UUID;

/**
 * @author LevelX2
 */
public final class ScaleguardSentinels extends CardImpl {

    public ScaleguardSentinels(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{G}{G}");
        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.SOLDIER);
        this.power = new MageInt(2);
        this.toughness = new MageInt(3);

        // As an additional cost to cast Scaleguard Sentinels, you may reveal a Dragon card from your hand.
        this.getSpellAbility().addCost(new RevealDragonFromHandCost());

        // Scaleguard Sentinels enters the battlefield with a +1/+1 counter on it if you revealed a Dragon card or controlled a Dragon as you cast Scaleguard Sentinels.
        this.addAbility(new SimpleStaticAbility(new ConditionalReplacementEffect(
                new EntersWithCountersEffect(CounterType.P1P1.createInstance()),
                RevealedOrControlledDragonCondition.instance)
                .setText("{this} enters with a +1/+1 counter on it if you revealed a Dragon card " +
                        "or controlled a Dragon as you cast this spell")
                ), new DragonOnTheBattlefieldWhileSpellWasCastWatcher()
        );
    }

    private ScaleguardSentinels(final ScaleguardSentinels card) {
        super(card);
    }

    @Override
    public ScaleguardSentinels copy() {
        return new ScaleguardSentinels(this);
    }
}
