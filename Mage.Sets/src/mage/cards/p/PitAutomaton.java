package mage.cards.p;

import mage.MageInt;
import mage.Mana;
import mage.abilities.Ability;
import mage.abilities.DelayedTriggeredAbility;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.costs.mana.GenericManaCost;
import mage.abilities.effects.common.CopyTargetStackObjectEffect;
import mage.abilities.effects.common.CreateDelayedTriggeredAbilityEffect;
import mage.abilities.keyword.DefenderAbility;
import mage.abilities.keyword.ExhaustAbility;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.abilities.mana.conditional.FilteredAbilityManaCondition;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.SubType;
import mage.filter.StaticTypedFilters;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.game.stack.StackObject;
import mage.target.targetpointer.FixedTarget;

import java.util.UUID;

/**
 * @author TheElk801
 */
public final class PitAutomaton extends CardImpl {

    public PitAutomaton(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT, CardType.CREATURE}, "{2}");

        this.subtype.add(SubType.CONSTRUCT);
        this.power = new MageInt(0);
        this.toughness = new MageInt(4);

        // Defender
        this.addAbility(DefenderAbility.getInstance());

        // {T}: Add {C}{C}. Spend this mana only to activate abilities.
        this.addAbility(ComposedManaAbilityBuilder.builder()
                .cost(new TapSourceCost())
                .addStatic(Mana.ColorlessMana(2))
                .condition(new FilteredAbilityManaCondition(StaticTypedFilters.ACTIVATED_ABILITY))
                .ruleText("Add {C}{C}. Spend this mana only to activate abilities")
                .build()
        );

        // {2}, {T}: When you next activate an exhaust ability this turn, copy it. You may choose new targets for the copy.
        Ability ability = new SimpleActivatedAbility(
                new CreateDelayedTriggeredAbilityEffect(new PitAutomatonTriggeredAbility()), new GenericManaCost(2)
        );
        ability.addCost(new TapSourceCost());
        this.addAbility(ability);
    }

    private PitAutomaton(final PitAutomaton card) {
        super(card);
    }

    @Override
    public PitAutomaton copy() {
        return new PitAutomaton(this);
    }
}

class PitAutomatonTriggeredAbility extends DelayedTriggeredAbility {

    PitAutomatonTriggeredAbility() {
        super(new CopyTargetStackObjectEffect(true), Duration.EndOfTurn, true, false);
    }

    private PitAutomatonTriggeredAbility(final PitAutomatonTriggeredAbility ability) {
        super(ability);
    }

    @Override
    public PitAutomatonTriggeredAbility copy() {
        return new PitAutomatonTriggeredAbility(this);
    }

    @Override
    public boolean checkEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.ACTIVATED_ABILITY;
    }

    @Override
    public boolean checkTrigger(GameEvent event, Game game) {
        if (!isControlledBy(event.getPlayerId())) {
            return false;
        }
        StackObject stackObject = game.getStack().getStackObject(event.getTargetId());
        if (stackObject == null || !(stackObject.getStackAbility() instanceof ExhaustAbility)) {
            return false;
        }
        this.getEffects().setTargetPointer(new FixedTarget(event.getTargetId()));
        return true;
    }

    @Override
    public String getRule() {
        return "When you next activate an exhaust ability that isn't a mana ability this turn, copy it. You may choose new targets for the copy.";
    }
}
