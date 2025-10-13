package mage.cards.m;

import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.condition.common.FirstTimeCreateTokensCondition;
import mage.abilities.decorator.ConditionalReplacementEffect;
import mage.abilities.effects.ReplacementEffect;
import mage.abilities.effects.common.AttachEffect;
import mage.abilities.effects.common.replacement.ReplaceTokenEffect;
import mage.abilities.keyword.EnchantAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.filter.StaticFilters;
import mage.target.TargetPermanent;
import mage.watchers.common.CreatedTokenWatcher;

import java.util.UUID;

/**
 * @author TheElk801
 */
public final class MoonlitMeditation extends CardImpl {

    public MoonlitMeditation(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ENCHANTMENT}, "{2}{U}");

        this.subtype.add(SubType.AURA);

        // Enchant artifact or creature you control
        TargetPermanent auraTarget = new TargetPermanent(StaticFilters.FILTER_CONTROLLED_PERMANENT_ARTIFACT_OR_CREATURE);
        this.getSpellAbility().addTarget(auraTarget);
        this.getSpellAbility().addEffect(new AttachEffect(Outcome.BoostCreature));
        this.addAbility(new EnchantAbility(auraTarget));

        // The first time you would create one or more tokens each turn, you may instead create that many tokens that are copies of enchanted permanent.
        ReplacementEffect effect = new ReplaceTokenEffect(ReplaceTokenEffect.ModificationType.REPLACE_ATTACHED, 0, null)
                .setOptional(true);
        effect.setText("the first time you would create one or more tokens each turn, " +
                        "you may instead create that many tokens that are copies of enchanted permanent");
        this.addAbility(new SimpleStaticAbility(
                new ConditionalReplacementEffect(effect, FirstTimeCreateTokensCondition.EACH_TURN)
        ), new CreatedTokenWatcher());
    }

    private MoonlitMeditation(final MoonlitMeditation card) {
        super(card);
    }

    @Override
    public MoonlitMeditation copy() {
        return new MoonlitMeditation(this);
    }
}
