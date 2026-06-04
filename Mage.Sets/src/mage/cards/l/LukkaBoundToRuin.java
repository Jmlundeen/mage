package mage.cards.l;

import mage.abilities.Ability;
import mage.abilities.LoyaltyAbility;
import mage.abilities.dynamicvalue.common.GreatestAmongPermanentsValue;
import mage.abilities.effects.common.CreateTokenEffect;
import mage.abilities.effects.common.DamageMultiEffect;
import mage.abilities.keyword.CompleatedAbility;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.abilities.mana.conditional.SpendOrActivateManaCondition;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.ManaType;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.filter.StaticTypedFilters;
import mage.game.Game;
import mage.game.permanent.token.PhyrexianBeastToxicToken;
import mage.target.common.TargetCreatureOrPlaneswalkerAmount;
import mage.target.targetadjustment.TargetAdjuster;

import java.util.Set;
import java.util.UUID;

/**
 * @author miesma
 */
public class LukkaBoundToRuin extends CardImpl {

    public LukkaBoundToRuin(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.PLANESWALKER}, "{2}{R}{R/G/P}{G}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.LUKKA);
        this.setStartingLoyalty(5);

        // Compleated
        this.addAbility(CompleatedAbility.getInstance());

        // +1: Add {R}{G}. Spend this mana only to cast creature spells or activate abilities of creatures.
        Ability ability = new LoyaltyAbility(new ComposedManaAbilityBuilder()
                .addStatic(Set.of(ManaType.RED, ManaType.GREEN), 1)
                .condition(new SpendOrActivateManaCondition(StaticTypedFilters.A_CREATURE_CARD))
                .ruleText("Add {R}{G}. Spend this mana only to cast creature spells or activate abilities of creatures")
                .buildEffect(), 1);
        this.addAbility(ability);

        // −1: Create a 3/3 green Phyrexian Beast creature token with toxic 1.
        ability = new LoyaltyAbility(new CreateTokenEffect(new PhyrexianBeastToxicToken()), -1);
        this.addAbility(ability);

        // −4: Lukka deals X damage divided as you choose among any number of target creatures and/or planeswalkers,
        // where X is the greatest power among creatures you controlled as you activated this ability.
        DamageMultiEffect damageMultiEffect = new DamageMultiEffect();
        damageMultiEffect.setText("Lukka deals X damage divided as you choose " +
                "among any number of target creatures and/or planeswalkers, " +
                "where X is the greatest power among creatures you control as you activate this ability.");
        ability = new LoyaltyAbility(damageMultiEffect, -4);
        ability.setTargetAdjuster(LukkaBoundToRuinAdjuster.instance);
        ability.addHint(GreatestAmongPermanentsValue.POWER_CONTROLLED_CREATURES.getHint());
        this.addAbility(ability);
    }

    private LukkaBoundToRuin(final LukkaBoundToRuin card) {
        super(card);
    }

    @Override
    public LukkaBoundToRuin copy() {
        return new LukkaBoundToRuin(this);
    }
}

/**
 * Gatherer Rulings:
 * 04.02.2023
 * You can't choose more targets than the greatest power among creatures you control as you activate the ability,
 * and each chosen target must receive at least 1 damage.
 */
enum LukkaBoundToRuinAdjuster implements TargetAdjuster {
    instance;

    @Override
    public void adjustTargets(Ability ability, Game game) {
        // Maximum targets is equal to the damage - as each target need to be assigned at least 1 damage
        ability.getTargets().clear();
        int xValue = GreatestAmongPermanentsValue.POWER_CONTROLLED_CREATURES.calculate(game, ability, null);
        ability.addTarget(new TargetCreatureOrPlaneswalkerAmount(xValue, 0, xValue));
    }
}
