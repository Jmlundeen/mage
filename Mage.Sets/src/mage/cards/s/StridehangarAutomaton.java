package mage.cards.s;

import mage.MageInt;
import mage.MageObject;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.common.continuous.BoostControlledEffect;
import mage.abilities.effects.common.replacement.ReplaceTokenEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.SubType;
import mage.filter.common.FilterCreaturePermanent;
import mage.game.permanent.token.ThopterColorlessToken;

import java.util.UUID;


/**
 * @author grimreap124
 */
public final class StridehangarAutomaton extends CardImpl {

    private static final FilterCreaturePermanent filter = new FilterCreaturePermanent(SubType.THOPTER, "Thopters");

    public StridehangarAutomaton(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT, CardType.CREATURE}, "{3}");
        
        this.subtype.add(SubType.CONSTRUCT);
        this.power = new MageInt(1);
        this.toughness = new MageInt(4);

        // Thopters you control get +1/+1.
        this.addAbility(new SimpleStaticAbility(new BoostControlledEffect(
                1, 1, Duration.WhileOnBattlefield, filter
        )));

        // If one or more artifact tokens would be created under your control, those tokens plus an additional 1/1 colorless Thopter artifact creature token with flying are created instead.
        this.addAbility(new SimpleStaticAbility(new ReplaceTokenEffect(ReplaceTokenEffect.ModificationType.ADD, 1, new ThopterColorlessToken())
                .withTokenCondition(MageObject::isArtifact)
                .setText("If one or more artifact tokens would be created under your control, those tokens plus an additional 1/1 colorless Thopter artifact creature token with flying are created instead")
        ));
    }

    private StridehangarAutomaton(final StridehangarAutomaton card) {
        super(card);
    }

    @Override
    public StridehangarAutomaton copy() {
        return new StridehangarAutomaton(this);
    }
}
