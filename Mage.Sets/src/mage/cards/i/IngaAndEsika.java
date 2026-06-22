package mage.cards.i;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.common.SpellCastControllerTriggeredAbility;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.effects.common.DrawCardSourceControllerEffect;
import mage.abilities.effects.common.continuous.GainAbilityControlledEffect;
import mage.abilities.keyword.VigilanceAbility;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.abilities.mana.conditional.FilteredSpellManaCondition;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.filter.FilterSpell;
import mage.filter.StaticFilters;
import mage.filter.StaticTypedFilters;
import mage.filter.common.FilterCreatureSpell;
import mage.filter.predicate.Predicate;
import mage.game.Game;
import mage.game.stack.StackObject;
import mage.watchers.common.ManaPaidSourceWatcher;

import java.util.UUID;

/**
 * @author TheElk801
 */
public final class IngaAndEsika extends CardImpl {

    private static final FilterSpell filter
            = new FilterCreatureSpell("a creature spell, if three or more mana from creatures was spent to cast it");

    static {
        filter.add(IngaAndEsikaPredicate.instance);
    }

    public IngaAndEsika(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{2}{G}{U}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.GOD);
        this.power = new MageInt(4);
        this.toughness = new MageInt(4);

        // Creatures you control have vigilance and "{T}: Add one mana of any color. Spend this mana only to cast a creature spell."
        String ruleText = "Add one mana of any color. Spend this mana only to cast a creature spell";
        Ability manaAbility = ComposedManaAbilityBuilder.builder()
                .cost(new TapSourceCost())
                .addAnyColor(1)
                .condition(new FilteredSpellManaCondition(StaticTypedFilters.A_CREATURE_SPELL))
                .ruleText(ruleText)
                .build();
        Ability ability = new SimpleStaticAbility(new GainAbilityControlledEffect(
                VigilanceAbility.getInstance(), Duration.WhileOnBattlefield,
                StaticFilters.FILTER_PERMANENT_CREATURES
        ));
        ability.addEffect(new GainAbilityControlledEffect(
                manaAbility,
                Duration.WhileOnBattlefield, StaticFilters.FILTER_PERMANENT_CREATURE
        ).setText(String.format("and \"%s\"", ruleText)));
        this.addAbility(ability);

        // Whenever you cast a creature spell, if three or more mana from creatures was spent to cast it, draw a card.
        this.addAbility(new SpellCastControllerTriggeredAbility(
                new DrawCardSourceControllerEffect(1), filter, false
        ));
    }

    private IngaAndEsika(final IngaAndEsika card) {
        super(card);
    }

    @Override
    public IngaAndEsika copy() {
        return new IngaAndEsika(this);
    }
}

enum IngaAndEsikaPredicate implements Predicate<StackObject> {
    instance;

    @Override
    public boolean apply(StackObject input, Game game) {
        return ManaPaidSourceWatcher.getCreaturePaid(input.getId(), game) >= 3;
    }
}
