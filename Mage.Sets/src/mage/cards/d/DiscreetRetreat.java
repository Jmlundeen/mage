package mage.cards.d;

import mage.abilities.Ability;
import mage.abilities.TriggeredAbilityImpl;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.effects.Effect;
import mage.abilities.effects.common.AttachEffect;
import mage.abilities.effects.common.DrawCardSourceControllerEffect;
import mage.abilities.effects.common.LoseLifeSourceControllerEffect;
import mage.abilities.effects.common.continuous.GainAbilityAttachedEffect;
import mage.abilities.keyword.EnchantAbility;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.abilities.mana.conditional.SpendOrActivateManaCondition;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.FilterTyped;
import mage.filter.predicate.mageobject.OutlawPredicate;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.game.stack.Spell;
import mage.target.TargetPermanent;
import mage.target.common.TargetLandPermanent;
import mage.watchers.common.SpellsCastWatcher;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * @author Susucr
 */
public final class DiscreetRetreat extends CardImpl {

    static final FilterTyped filter = new FilterTyped("outlaw")
            .add(mage.filter.predicate.typed.mageObject.object.OutlawPredicate.instance);

    public DiscreetRetreat(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ENCHANTMENT}, "{3}{B}");

        this.subtype.add(SubType.AURA);

        // Enchant land
        TargetPermanent auraTarget = new TargetLandPermanent();
        this.getSpellAbility().addTarget(auraTarget);
        this.getSpellAbility().addEffect(new AttachEffect(Outcome.Benefit));
        Ability ability = new EnchantAbility(auraTarget);
        this.addAbility(ability);

        // Enchanted land has "{T}: Add two mana of any one color. Spend this mana only to cast outlaw spells or activate abilities of outlaw sources."
        Ability gainedAbility = new ComposedManaAbilityBuilder()
                .cost(new TapSourceCost())
                .addChoiceAnyOneColor(2)
                .condition(new SpendOrActivateManaCondition(filter))
                .ruleText("Add two mana of any one color. Spend this mana only to cast outlaw spells or activate abilities of outlaw sources")
                .build();
        Effect effect = new GainAbilityAttachedEffect(gainedAbility, AttachmentType.AURA);
        effect.setText(String.format("Enchanted land has \"%s\"", gainedAbility.getRule()));
        this.addAbility(new SimpleStaticAbility(effect));

        // Whenever you cast your first outlaw spell each turn, you draw a card and you lose 1 life.
        this.addAbility(new DiscreetRetreatTriggeredAbility());
    }

    private DiscreetRetreat(final DiscreetRetreat card) {
        super(card);
    }

    @Override
    public DiscreetRetreat copy() {
        return new DiscreetRetreat(this);
    }
}

class DiscreetRetreatTriggeredAbility extends TriggeredAbilityImpl {

    DiscreetRetreatTriggeredAbility() {
        super(Zone.BATTLEFIELD, new DrawCardSourceControllerEffect(1, true));
        addEffect(new LoseLifeSourceControllerEffect(1).concatBy("and"));
        setTriggerPhrase("Whenever you cast your first outlaw spell each turn, ");
    }

    private DiscreetRetreatTriggeredAbility(final DiscreetRetreatTriggeredAbility ability) {
        super(ability);
    }

    @Override
    public DiscreetRetreatTriggeredAbility copy() {
        return new DiscreetRetreatTriggeredAbility(this);
    }

    @Override
    public boolean checkEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.SPELL_CAST;
    }

    @Override
    public boolean checkTrigger(GameEvent event, Game game) {
        if (!event.getPlayerId().equals(this.getControllerId())) {
            return false;
        }
        SpellsCastWatcher watcher = game.getState().getWatcher(SpellsCastWatcher.class);
        if (watcher == null) {
            return false;
        }
        List<Spell> outlawSpells = watcher
                .getSpellsCastThisTurn(this.getControllerId())
                .stream()
                .filter(Objects::nonNull)
                .filter(s -> OutlawPredicate.instance.apply(s, game))
                .toList();
        return outlawSpells.size() == 1 && outlawSpells.getFirst().getId().equals(event.getTargetId());
    }
}
