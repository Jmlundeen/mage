package mage.cards.c;

import mage.MageObject;
import mage.MageObjectReference;
import mage.abilities.Ability;
import mage.abilities.common.AsEntersBattlefieldAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.effects.ContinuousRuleModifyingEffectImpl;
import mage.abilities.effects.common.ChooseCreatureTypeEffect;
import mage.abilities.mana.ColorlessManaAbility;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.abilities.mana.providers.ChosenCreatureTypeConditionProvider;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.FilterTyped;
import mage.filter.predicate.typed.ability.type.SpellAbilityPredicate;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.game.stack.Spell;
import mage.watchers.Watcher;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 *
 * @author noxx
 */
public final class CavernOfSouls extends CardImpl {

    private static final FilterTyped filter = new FilterTyped("spell ability")
            .add(SpellAbilityPredicate.instance);

    public CavernOfSouls(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.LAND}, "");

        // As Cavern of Souls enters the battlefield, choose a creature type.
        this.addAbility(new AsEntersBattlefieldAbility(new ChooseCreatureTypeEffect(Outcome.BoostCreature)));

        // {T}: Add {C}.
        this.addAbility(new ColorlessManaAbility());

        // {T}: Add one mana of any color. Spend this mana only to cast a creature spell of the chosen type, and that spell can't be countered.
        Ability ability = ComposedManaAbilityBuilder.builder()
                .cost(new TapSourceCost())
                .addAnyColor(1)
                .runtimeCondition(new ChosenCreatureTypeConditionProvider(filter))
                .ruleText("Add one mana of any color. Spend this mana only to cast a creature spell of the chosen type, and that spell can't be countered")
                .build();
        this.addAbility(ability, new CavernOfSoulsWatcher(ability.getOriginalId()));
        this.addAbility(new SimpleStaticAbility(Zone.ALL, new CavernOfSoulsCantCounterEffect()).setRuleVisible(false));
    }

    private CavernOfSouls(final CavernOfSouls card) {
        super(card);
    }

    @Override
    public CavernOfSouls copy() {
        return new CavernOfSouls(this);
    }
}

class CavernOfSoulsWatcher extends Watcher {

    private final Set<MageObjectReference> spells = new HashSet<>();
    private final UUID originalId;

    public CavernOfSoulsWatcher(UUID originalId) {
        super(WatcherScope.CARD);
        this.originalId = originalId;
    }

    @Override
    public void watch(GameEvent event, Game game) {
        if (event.getType() == GameEvent.EventType.MANA_PAID) {
            if (event.getData() != null && event.getData().equals(originalId.toString()) && event.getTargetId() != null) {
                spells.add(new MageObjectReference(game.getObject(event.getTargetId()), game));
            }
        }
    }

    public boolean spellCantBeCountered(MageObjectReference mor) {
        return spells.contains(mor);
    }

    @Override
    public void reset() {
        super.reset();
        spells.clear();
    }
}

class CavernOfSoulsCantCounterEffect extends ContinuousRuleModifyingEffectImpl {

    CavernOfSoulsCantCounterEffect() {
        super(Duration.EndOfGame, Outcome.Benefit);
    }

    private CavernOfSoulsCantCounterEffect(final CavernOfSoulsCantCounterEffect effect) {
        super(effect);
    }

    @Override
    public CavernOfSoulsCantCounterEffect copy() {
        return new CavernOfSoulsCantCounterEffect(this);
    }

    @Override
    public String getInfoMessage(Ability source, GameEvent event, Game game) {
        MageObject sourceObject = game.getObject(source);
        if (sourceObject != null) {
            return "This spell can't be countered because a colored mana from " + sourceObject.getName() + " was spent to cast it.";
        }
        return null;
    }

    @Override
    public boolean checksEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.COUNTER;
    }

    @Override
    public boolean applies(GameEvent event, Ability source, Game game) {
        CavernOfSoulsWatcher watcher = game.getState().getWatcher(CavernOfSoulsWatcher.class, source.getSourceId());
        Spell spell = game.getStack().getSpell(event.getTargetId());
        return spell != null && watcher != null && watcher.spellCantBeCountered(new MageObjectReference(spell, game));
    }
}
