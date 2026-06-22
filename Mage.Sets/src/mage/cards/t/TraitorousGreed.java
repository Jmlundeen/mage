package mage.cards.t;

import mage.abilities.effects.common.UntapTargetEffect;
import mage.abilities.effects.common.continuous.generic.GenericContinuousEffect;
import mage.abilities.keyword.HasteAbility;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.Outcome;
import mage.target.common.TargetCreaturePermanent;

import java.util.UUID;

/**
 * @author TheElk801
 */
public final class TraitorousGreed extends CardImpl {

    public TraitorousGreed(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.SORCERY}, "{3}{R}");

        // Gain control of target creature until end of turn. Untap that creature. It gains haste until end of turn. Add two mana of any one color.
        this.getSpellAbility().addEffect(new GenericContinuousEffect(Duration.EndOfTurn, Outcome.GainControl)
                .withGainControl()
                .setText("Gain control of target creature until end of turn"));
        this.getSpellAbility().addEffect(new UntapTargetEffect().setText("Untap that creature"));
        this.getSpellAbility().addEffect(new GenericContinuousEffect(Duration.EndOfTurn, Outcome.AddAbility)
                .withGainedAbilities(HasteAbility.getInstance())
                .setText("It gains haste until end of turn"));
        this.getSpellAbility().addEffect(ComposedManaAbilityBuilder.builder()
                .addAnyColor(1)
                .ruleText("add two mana of any one color")
                .buildEffect());
        this.getSpellAbility().addTarget(new TargetCreaturePermanent());
    }

    private TraitorousGreed(final TraitorousGreed card) {
        super(card);
    }

    @Override
    public TraitorousGreed copy() {
        return new TraitorousGreed(this);
    }
}
