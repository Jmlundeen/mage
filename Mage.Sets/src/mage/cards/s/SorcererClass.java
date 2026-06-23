package mage.cards.s;

import mage.abilities.Ability;
import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.common.SpellCastControllerTriggeredAbility;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.dynamicvalue.common.InstantAndSorceryCastThisTurn;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.DrawDiscardControllerEffect;
import mage.abilities.effects.common.continuous.GainClassAbilitySourceEffect;
import mage.abilities.effects.common.continuous.generic.GenericContinuousEffect;
import mage.abilities.keyword.ClassLevelAbility;
import mage.abilities.keyword.ClassReminderAbility;
import mage.abilities.mana.ComposedManaAbility;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.abilities.mana.conditional.FilteredAbilityManaCondition;
import mage.abilities.mana.conditional.FilteredSpellManaCondition;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.Filter;
import mage.filter.FilterTyped;
import mage.filter.StaticFilters;
import mage.filter.StaticTypedFilters;
import mage.filter.predicate.typed.ability.IAbilityPredicate;
import mage.filter.predicate.typed.ability.type.AbilityPredicate;
import mage.game.Game;
import mage.game.stack.Spell;
import mage.players.Player;

import java.util.Set;
import java.util.UUID;

/**
 * @author TheElk801
 */
public final class SorcererClass extends CardImpl {

    private static final FilterTyped filter = new FilterTyped("class level ability")
            .addAll(
                    AbilityPredicate.instance,
                    (IAbilityPredicate) (osp, game) -> osp.getObject() instanceof ClassLevelAbility
            );

    public SorcererClass(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ENCHANTMENT}, "{U}{R}");

        this.subtype.add(SubType.CLASS);

        // (Gain the next level as a sorcery to add its ability.)
        this.addAbility(new ClassReminderAbility());

        // When Sorcerer Class enters the battlefield, draw two cards, then discard two cards.
        this.addAbility(new EntersBattlefieldTriggeredAbility(
                new DrawDiscardControllerEffect(2, 2)
        ));

        // {U}{R}: Level 2
        this.addAbility(new ClassLevelAbility(2, "{U}{R}"));

        // Creatures you control have "{T}: Add {U} or {R}. Spend this mana only to cast an instant or sorcery spell or to gain a Class level."
        ComposedManaAbility manaAbility = ComposedManaAbilityBuilder.builder()
                .cost(new TapSourceCost())
                .addChoice(Set.of(ManaType.BLUE, ManaType.RED), 1)
                .condition(new FilteredSpellManaCondition(StaticTypedFilters.AN_INSTANT_OR_SORCERY_SPELL))
                .condition(new FilteredAbilityManaCondition(filter))
                .comparisonScope(Filter.ComparisonScope.Any)
                .ruleText("Add {U} or {R}. Spend this mana only to cast an instant or sorcery spell or to gain a Class level")
                .build();
        Ability ability = new SimpleStaticAbility(new GenericContinuousEffect(Outcome.AddAbility, StaticTypedFilters.CREATURE_YOU_CONTROL)
                .withGainedAbilities(manaAbility)
                .setText("creatures you control have \"{T}: Add {U} or {R}. Spend this mana only to cast an instant or sorcery spell or to gain a Class level.\""));
        this.addAbility(new SimpleStaticAbility(new GainClassAbilitySourceEffect(ability, 2)));

        // {3}{U}{R}: Level 3
        this.addAbility(new ClassLevelAbility(3, "{3}{U}{R}"));

        // Whenever you cast an instant or sorcery spell, that spell deals damage to each opponent equal to the number of instant or sorcery spells you've cast this turn.
        this.addAbility(new SimpleStaticAbility(new GainClassAbilitySourceEffect(
                new SpellCastControllerTriggeredAbility(
                        new SorcererClassEffect(),
                        StaticFilters.FILTER_SPELL_AN_INSTANT_OR_SORCERY,
                        false, SetTargetPointer.SPELL
                ), 3
        )).addHint(InstantAndSorceryCastThisTurn.YOU.getHint()));
    }

    private SorcererClass(final SorcererClass card) {
        super(card);
    }

    @Override
    public SorcererClass copy() {
        return new SorcererClass(this);
    }
}

class SorcererClassEffect extends OneShotEffect {

    SorcererClassEffect() {
        super(Outcome.Benefit);
        staticText = "that spell deals damage to each opponent equal " +
                "to the number of instant and sorcery spells you've cast this turn";
    }

    private SorcererClassEffect(final SorcererClassEffect effect) {
        super(effect);
    }

    @Override
    public SorcererClassEffect copy() {
        return new SorcererClassEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Spell spell = (Spell) getValue("spellCast");
        if (spell == null) {
            return false;
        }
        int count = InstantAndSorceryCastThisTurn.YOU.calculate(game, source, this);
        if (count < 1) {
            return false;
        }
        for (UUID playerId : game.getOpponents(source.getControllerId())) {
            Player player = game.getPlayer(playerId);
            if (player == null) {
                continue;
            }
            player.damage(count, spell.getId(), source, game);
        }
        return true;
    }
}
