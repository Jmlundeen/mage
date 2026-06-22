package mage.cards.a;

import mage.MageInt;
import mage.Mana;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.abilities.mana.conditional.SpendOrActivateManaCondition;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.filter.FilterTyped;
import mage.filter.predicate.typed.Spell.SpellPredicate;
import mage.filter.predicate.typed.ability.type.ActivatedAbilityPredicate;

import java.util.UUID;

/**
 * @author TheElk801
 */
public final class AutomatedArtificer extends CardImpl {

    private static final FilterTyped filter = new FilterTyped("artifact spell or activated ability")
            .addAll(
                    CardType.ARTIFACT.getPredicate(),
                    SpellPredicate.instance
            )
            .add(ActivatedAbilityPredicate.instance);

    public AutomatedArtificer(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT, CardType.CREATURE}, "{2}");

        this.subtype.add(SubType.ARTIFICER);
        this.power = new MageInt(1);
        this.toughness = new MageInt(3);

        // {T}: Add {C}. Spend this mana only to activate an ability or cast an artifact spell.
        this.addAbility(ComposedManaAbilityBuilder.builder()
                .cost(new TapSourceCost())
                .addStatic(Mana.ColorlessMana(1))
                .condition(new SpendOrActivateManaCondition(filter))
                .ruleText("Add {C}. Spend this mana only to activate an ability or cast an artifact spell")
                .build()
        );
    }

    private AutomatedArtificer(final AutomatedArtificer card) {
        super(card);
    }

    @Override
    public AutomatedArtificer copy() {
        return new AutomatedArtificer(this);
    }
}
