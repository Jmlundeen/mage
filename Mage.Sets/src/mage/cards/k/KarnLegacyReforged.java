package mage.cards.k;

import mage.MageInt;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.dynamicvalue.common.GreatestAmongPermanentsValue;
import mage.abilities.dynamicvalue.common.PermanentsOnBattlefieldCount;
import mage.abilities.effects.common.continuous.SetBasePowerToughnessSourceEffect;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.abilities.mana.conditional.FilteredSpellManaCondition;
import mage.abilities.mana.conditional.InvertedManaCondition;
import mage.abilities.triggers.BeginningOfUpkeepTriggeredAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.StaticFilters;
import mage.filter.StaticTypedFilters;

import java.util.UUID;

/**
 * @author TheElk801
 */
public final class KarnLegacyReforged extends CardImpl {

    public KarnLegacyReforged(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT, CardType.CREATURE}, "{5}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.GOLEM);
        this.power = new MageInt(0);
        this.toughness = new MageInt(0);

        // Karn, Legacy Reforged's power and toughness are each equal to the greatest mana value among artifacts you control.
        this.addAbility(new SimpleStaticAbility(
                Zone.ALL, new SetBasePowerToughnessSourceEffect(GreatestAmongPermanentsValue.MANAVALUE_CONTROLLED_ARTIFACTS)
                .setText("{this}'s power and toughness are each equal to the greatest mana value among artifacts you control")
        ));

        // At the beginning of your upkeep, add {C} for each artifact you control. This mana can't be spent to cast nonartifact spells. Until end of turn, you don't lose this mana as steps and phases end.
        this.addAbility(new BeginningOfUpkeepTriggeredAbility(
                ComposedManaAbilityBuilder.builder()
                        .addDynamic(new PermanentsOnBattlefieldCount(StaticFilters.FILTER_CONTROLLED_PERMANENT_ARTIFACT), ManaType.COLORLESS)
                        .condition(new InvertedManaCondition(new FilteredSpellManaCondition(StaticTypedFilters.A_NON_ARTIFACT_SPELL)))
                        .duration(Duration.EndOfTurn)
                        .ruleText("add {C} for each artifact you control. This mana can't be spent to cast nonartifact spells. Until end of turn, you don't lose this mana as steps and phases end")
                        .buildEffect()
        ));
    }

    private KarnLegacyReforged(final KarnLegacyReforged card) {
        super(card);
    }

    @Override
    public KarnLegacyReforged copy() {
        return new KarnLegacyReforged(this);
    }
}
