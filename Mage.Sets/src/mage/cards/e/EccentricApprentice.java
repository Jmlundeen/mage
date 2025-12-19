package mage.cards.e;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.condition.common.CompletedDungeonCondition;
import mage.abilities.effects.common.continuous.BecomesCreatureTypeTargetEffect;
import mage.abilities.effects.common.continuous.GainAbilityTargetEffect;
import mage.abilities.effects.common.continuous.SetBasePowerToughnessTargetEffect;
import mage.abilities.effects.keyword.VentureIntoTheDungeonEffect;
import mage.abilities.hint.common.CurrentDungeonHint;
import mage.abilities.keyword.FlyingAbility;
import mage.abilities.triggers.BeginningOfCombatTriggeredAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.target.common.TargetCreaturePermanent;
import mage.watchers.common.CompletedDungeonWatcher;

import java.util.UUID;

/**
 * @author TheElk801
 */
public final class EccentricApprentice extends CardImpl {

    public EccentricApprentice(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{2}{U}");

        this.subtype.add(SubType.TIEFLING);
        this.subtype.add(SubType.WIZARD);
        this.power = new MageInt(2);
        this.toughness = new MageInt(2);

        // Flying
        this.addAbility(FlyingAbility.getInstance());

        // When Eccentric Apprentice enters the battlefield, venture into the dungeon.
        this.addAbility(new EntersBattlefieldTriggeredAbility(new VentureIntoTheDungeonEffect())
                .addHint(CurrentDungeonHint.instance));

        // At the beginning of combat on your turn, if you've completed a dungeon, up to one target creature becomes a Bird with base power and toughness 1/1 and flying until end of turn.
        Ability ability = new BeginningOfCombatTriggeredAbility(new BecomesCreatureTypeTargetEffect(Duration.EndOfTurn, SubType.BIRD)
                .setText("up to one target creature becomes a Bird"))
                .withInterveningIf(CompletedDungeonCondition.instance).addHint(CompletedDungeonCondition.getHint());
        ability.addTarget(new TargetCreaturePermanent(0, 1));
        ability.addEffect(new SetBasePowerToughnessTargetEffect(1, 1, Duration.EndOfTurn)
                .setText("with base power and toughness 1/1"));
        ability.addEffect(new GainAbilityTargetEffect(FlyingAbility.getInstance())
                .setText("flying until end of turn").concatBy("and"));
        this.addAbility(ability, new CompletedDungeonWatcher());
    }

    private EccentricApprentice(final EccentricApprentice card) {
        super(card);
    }

    @Override
    public EccentricApprentice copy() {
        return new EccentricApprentice(this);
    }
}
