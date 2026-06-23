package mage.cards.m;

import mage.ConditionalMana;
import mage.MageInt;
import mage.MageObject;
import mage.Mana;
import mage.abilities.Ability;
import mage.abilities.SpellAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.condition.Condition;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.effects.common.continuous.LookAtTopCardOfLibraryAnyTimeEffect;
import mage.abilities.effects.common.continuous.PlayFromTopOfLibraryEffect;
import mage.abilities.effects.common.continuous.generic.GenericContinuousEffect;
import mage.abilities.keyword.FlyingAbility;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.abilities.mana.builder.ConditionalManaBuilder;
import mage.abilities.mana.conditional.FilteredSpellManaCondition;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.FilterCard;
import mage.filter.FilterTyped;
import mage.filter.StaticTypedFilters;
import mage.filter.common.FilterArtifactCard;
import mage.filter.predicate.typed.LogicalPredicate;
import mage.filter.predicate.typed.Spell.SpellCastFromZonePredicate;
import mage.filter.predicate.typed.Spell.SpellPredicate;
import mage.game.Game;
import mage.game.stack.Spell;

import java.util.UUID;

/**
 * @author TheElk801
 */
public final class MmmenonTheRightHand extends CardImpl {

    private static final FilterCard filter = new FilterArtifactCard("cast artifact spells");
    private static final FilterTyped nonHandSpellFilter = new FilterTyped("a spell from anywhere other than your hand")
            .addAll(
                    SpellPredicate.instance,
                    LogicalPredicate.not(SpellCastFromZonePredicate.HAND)
            );

    public MmmenonTheRightHand(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{3}{U}{U}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.JELLYFISH);
        this.subtype.add(SubType.ADVISOR);
        this.power = new MageInt(3);
        this.toughness = new MageInt(4);

        // Flying
        this.addAbility(FlyingAbility.getInstance());

        // You may look at the top card of your library any time.
        this.addAbility(new SimpleStaticAbility(new LookAtTopCardOfLibraryAnyTimeEffect()));

        // You may cast artifact spells from the top of your library.
        this.addAbility(new SimpleStaticAbility(new PlayFromTopOfLibraryEffect(filter)));

        // Artifacts you control have "{T}: Add {U}. Spend this mana only to cast a spell from anywhere other than your hand."
        this.addAbility(new SimpleStaticAbility(new GenericContinuousEffect(Outcome.AddAbility, StaticTypedFilters.ARTIFACT_YOU_CONTROL)
                .withGainedAbilities(ComposedManaAbilityBuilder.builder()
                        .cost(new TapSourceCost())
                        .addStatic(Mana.BlueMana(1))
                        .condition(new FilteredSpellManaCondition(nonHandSpellFilter))
                        .ruleText("Add {U}. Spend this mana only to cast a spell from anywhere other than your hand")
                        .build()
                )
                .setText("Artifacts you control have \"{T}: Add {U}. Spend this mana only to cast a spell from anywhere other than your hand.\"")
        ));
    }

    private MmmenonTheRightHand(final MmmenonTheRightHand card) {
        super(card);
    }

    @Override
    public MmmenonTheRightHand copy() {
        return new MmmenonTheRightHand(this);
    }
}

class MmmenonTheRightHandManaBuilder extends ConditionalManaBuilder {

    @Override
    public ConditionalMana build(Object... options) {
        return new MmmenonTheRightHandConditionalMana(this.mana);
    }

    @Override
    public String getRule() {
        return "Spend this mana only to cast a spell from anywhere other than your hand";
    }
}

class MmmenonTheRightHandConditionalMana extends ConditionalMana {

    public MmmenonTheRightHandConditionalMana(Mana mana) {
        super(mana);
        this.staticText = "Spend this mana only to cast a spell from anywhere other than your hand";
        addCondition(MmmenonTheRightHandManaCondition.instance);
    }
}

enum MmmenonTheRightHandManaCondition implements Condition {
    instance;

    @Override
    public boolean apply(Game game, Ability source) {
        if (!(source instanceof SpellAbility)) {
            return false;
        }
        MageObject object = game.getObject(source);
        if (!source.isControlledBy(game.getOwnerId(object))) {
            return false;
        }
        if (object instanceof Spell) {
            return !Zone.HAND.match(((Spell) object).getFromZone());
        }
        // checking mana without real cast
        return game.inCheckPlayableState() && !Zone.HAND.match(game.getState().getZone(source.getSourceId()));
    }
}
