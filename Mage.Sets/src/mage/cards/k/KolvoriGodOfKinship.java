package mage.cards.k;

import mage.MageInt;
import mage.Mana;
import mage.abilities.Ability;
import mage.abilities.common.AsEntersBattlefieldAbility;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.condition.common.PermanentsOnTheBattlefieldCondition;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.decorator.ConditionalContinuousEffect;
import mage.abilities.effects.common.ChooseCreatureTypeEffect;
import mage.abilities.effects.common.LookLibraryAndPickControllerEffect;
import mage.abilities.effects.common.continuous.BoostSourceEffect;
import mage.abilities.effects.common.continuous.GainAbilitySourceEffect;
import mage.abilities.keyword.VigilanceAbility;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.abilities.mana.conditional.FilteredSpellManaCondition;
import mage.abilities.mana.providers.ChosenCreatureTypeConditionProvider;
import mage.cards.CardSetInfo;
import mage.cards.ModalDoubleFacedCard;
import mage.constants.*;
import mage.filter.Filter;
import mage.filter.FilterTyped;
import mage.filter.common.FilterCreatureCard;
import mage.filter.common.FilterCreaturePermanent;
import mage.filter.predicate.typed.ability.type.SpellAbilityPredicate;
import mage.filter.predicate.typed.mageObject.IMageObjectPredicate;

import java.util.UUID;

/**
 * @author weirddan455
 */
public final class KolvoriGodOfKinship extends ModalDoubleFacedCard {

    private static final FilterCreaturePermanent filter = new FilterCreaturePermanent();
    private static final FilterCreatureCard filter2 = new FilterCreatureCard("a legendary creature card");
    private static final FilterTyped spellAbilityFilter = new FilterTyped("spell ability")
            .add(SpellAbilityPredicate.instance);
    private static final FilterTyped legendaryFilter = new FilterTyped("legendary creature spell")
            .addAll(
                    IMageObjectPredicate.getOSPPredicate(CardType.CREATURE.getPredicate()),
                    IMageObjectPredicate.getOSPPredicate(SuperType.LEGENDARY.getPredicate())
            );

    static {
        filter.add(IMageObjectPredicate.getOSPPredicate(SuperType.LEGENDARY.getPredicate()));
        filter2.add(IMageObjectPredicate.getOSPPredicate(SuperType.LEGENDARY.getPredicate()));
    }

    private static final PermanentsOnTheBattlefieldCondition condition
            = new PermanentsOnTheBattlefieldCondition(filter, ComparisonType.MORE_THAN, 2, true);

    public KolvoriGodOfKinship(UUID ownerId, CardSetInfo setInfo) {
        super(
                ownerId, setInfo,
                new SuperType[]{SuperType.LEGENDARY}, new CardType[]{CardType.CREATURE}, new SubType[]{SubType.GOD}, "{2}{G}{G}",
                "The Ringhart Crest",
                new SuperType[]{SuperType.LEGENDARY}, new CardType[]{CardType.ARTIFACT}, new SubType[]{}, "{1}{G}"
        );

        // 1.
        // Kolvori, God of Kinship
        // Legendary Creature - God
        this.getLeftHalfCard().setPT(new MageInt(2), new MageInt(4));

        // As long as you control three or more legendary creatures, Kolvori, God of Kinship gets +4/+2 and has vigilance.
        Ability ability = new SimpleStaticAbility(new ConditionalContinuousEffect(
                new BoostSourceEffect(4, 2, Duration.WhileOnBattlefield), condition,
                "As long as you control three or more legendary creatures, {this} gets +4/+2"
        ));
        ability.addEffect(new ConditionalContinuousEffect(
                new GainAbilitySourceEffect(VigilanceAbility.getInstance()), condition,
                "and has vigilance"
        ));
        this.getLeftHalfCard().addAbility(ability);

        // {1}{G}, {T}: Look at the top six cards of your library.
        // You may reveal a legendary creature card from among them and put it into your hand.
        // Put the rest on the bottom of your library in a random order.
        ability = new SimpleActivatedAbility(
                new LookLibraryAndPickControllerEffect(6, 1, filter2, PutCards.HAND, PutCards.BOTTOM_RANDOM),
                new ManaCostsImpl<>("{1}{G}"));
        ability.addCost(new TapSourceCost());
        this.getLeftHalfCard().addAbility(ability);

        // 2.
        // The Ringhart Crest
        // Legendary Artifact
        // As The Ringhart Crest enters the battlefield, choose a creature type.
        this.getRightHalfCard().addAbility(new AsEntersBattlefieldAbility(new ChooseCreatureTypeEffect(Outcome.Benefit)));

        // {T}: Add {G}. Spend this mana only to cast a creature spell of the chosen type or a legendary creature spell.
        this.getRightHalfCard().addAbility(ComposedManaAbilityBuilder.builder()
                .cost(new TapSourceCost())
                .addStatic(Mana.GreenMana(1))
                .runtimeCondition(new ChosenCreatureTypeConditionProvider(spellAbilityFilter))
                .condition(new FilteredSpellManaCondition(legendaryFilter))
                .comparisonScope(Filter.ComparisonScope.Any)
                .ruleText("Add {G}. Spend this mana only to cast a creature spell of the chosen type or a legendary creature spell")
                .build()
        );
    }

    private KolvoriGodOfKinship(final KolvoriGodOfKinship card) {
        super(card);
    }

    @Override
    public KolvoriGodOfKinship copy() {
        return new KolvoriGodOfKinship(this);
    }
}
