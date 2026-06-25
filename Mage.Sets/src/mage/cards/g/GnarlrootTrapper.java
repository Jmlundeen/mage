package mage.cards.g;

import mage.MageInt;
import mage.Mana;
import mage.abilities.Ability;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.costs.common.PayLifeCost;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.effects.Effect;
import mage.abilities.effects.common.continuous.generic.GenericContinuousEffect;
import mage.abilities.keyword.DeathtouchAbility;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.abilities.mana.conditional.FilteredSpellManaCondition;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.FilterTyped;
import mage.filter.common.FilterControlledPermanent;
import mage.filter.predicate.permanent.AttackingPredicate;
import mage.filter.predicate.typed.Spell.SpellPredicate;
import mage.filter.predicate.typed.mageObject.IMageObjectPredicate;
import mage.target.common.TargetControlledPermanent;

import java.util.UUID;

/**
 * @author LevelX2
 */
public final class GnarlrootTrapper extends CardImpl {

    private static final FilterControlledPermanent filter = new FilterControlledPermanent("attacking ELf you control");
    private static final FilterTyped filterSpell = new FilterTyped("Elf creature spell")
            .addAll(
                    SpellPredicate.instance,
                    IMageObjectPredicate.getOSPPredicate(SubType.ELF.getPredicate())
            );

    static {
        filter.add(AttackingPredicate.instance);
        filter.add(SubType.ELF.getPredicate());
    }

    public GnarlrootTrapper(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{B}");
        this.subtype.add(SubType.ELF);
        this.subtype.add(SubType.DRUID);
        this.power = new MageInt(1);
        this.toughness = new MageInt(1);

        // {T}, Pay 1 life: Add {G}. Spend this mana only to cast an Elf creature spell.
        this.addAbility(ComposedManaAbilityBuilder.builder()
                .cost(new TapSourceCost())
                .cost(new PayLifeCost(1))
                .addStatic(Mana.GreenMana(1))
                .condition(new FilteredSpellManaCondition(filterSpell))
                .ruleText("{T}, Pay 1 life: Add {G}. Spend this mana only to cast an Elf creature spell")
                .build()
        );

        // {T}: Target attacking Elf you control gains deathtouch until end of turn.
        Effect effect = new GenericContinuousEffect(Duration.EndOfTurn, Outcome.AddAbility)
                .setAffected(ContinuousAffected.STATIC_OR_DYNAMIC)
                .withGainedAbilities(DeathtouchAbility.getInstance())
                .setText("Target attacking Elf you control gains deathtouch until end of turn. <i>(Any amount of damage it deals to a creature is enough to destroy it.)</i>");
        Ability ability = new SimpleActivatedAbility(effect, new TapSourceCost());
        ability.addTarget(new TargetControlledPermanent(filter));
        this.addAbility(ability);

    }

    private GnarlrootTrapper(final GnarlrootTrapper card) {
        super(card);
    }

    @Override
    public GnarlrootTrapper copy() {
        return new GnarlrootTrapper(this);
    }
}
