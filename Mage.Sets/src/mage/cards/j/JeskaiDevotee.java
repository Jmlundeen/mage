package mage.cards.j;

import mage.MageInt;
import mage.abilities.ActivatedAbility;
import mage.abilities.common.FlurryAbility;
import mage.abilities.costs.mana.GenericManaCost;
import mage.abilities.effects.common.continuous.BoostSourceEffect;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.ManaType;
import mage.constants.SubType;

import java.util.Set;
import java.util.UUID;

/**
 * @author TheElk801
 */
public final class JeskaiDevotee extends CardImpl {

    public JeskaiDevotee(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{1}{R}");

        this.subtype.add(SubType.ORC);
        this.subtype.add(SubType.MONK);
        this.power = new MageInt(2);
        this.toughness = new MageInt(2);

        // Flurry -- Whenever you cast your second spell each turn, this creature gets +1/+1 until end of turn.
        this.addAbility(new FlurryAbility(new BoostSourceEffect(1, 1, Duration.EndOfTurn)));

        // {1}: Add {U}, {R}, or {W}. Activate only once each turn.
        ActivatedAbility ability = ComposedManaAbilityBuilder.builder()
                .cost(new GenericManaCost(1))
                .addChoice(Set.of(ManaType.BLUE, ManaType.RED, ManaType.WHITE), 1)
                .ruleText("Add {U}, {R}, or {W}. Activate only once each turn.")
                .maxActivations(1)
                .build();
        this.addAbility(ability);
    }

    private JeskaiDevotee(final JeskaiDevotee card) {
        super(card);
    }

    @Override
    public JeskaiDevotee copy() {
        return new JeskaiDevotee(this);
    }
}
