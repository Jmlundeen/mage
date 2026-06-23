package mage.cards.n;

import mage.abilities.Ability;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.costs.mana.GenericManaCost;
import mage.abilities.dynamicvalue.common.DevotionCount;
import mage.abilities.effects.Effect;
import mage.abilities.mana.ColorlessManaAbility;
import mage.abilities.mana.ComposedManaAbilityBuilder;
import mage.abilities.mana.providers.ManaTypeAmountProvider;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.ManaType;
import mage.constants.SuperType;
import mage.game.Game;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author LevelX2
 */
public final class NykthosShrineToNyx extends CardImpl {

    public NykthosShrineToNyx(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.LAND}, "");
        this.supertype.add(SuperType.LEGENDARY);

        // {T}: Add {C}.
        this.addAbility(new ColorlessManaAbility());

        // {2}, {T}: Choose a color. Add an amount of mana of that color equal to your devotion to that color.
        Ability ability = new ComposedManaAbilityBuilder()
                .cost(new GenericManaCost(2))
                .cost(new TapSourceCost())
                .addDynamicChoice(NykthosManaAmounts.instance)
                .ruleText("Choose a color. Add an amount of mana of that color equal to your devotion to that color. <i>(Your devotion to a color is the number of mana symbols of that color in the mana costs of permanents you control.)</i>")
                .build();
        ability.addHint(DevotionCount.W.getHint());
        ability.addHint(DevotionCount.U.getHint());
        ability.addHint(DevotionCount.B.getHint());
        ability.addHint(DevotionCount.R.getHint());
        ability.addHint(DevotionCount.G.getHint());
        this.addAbility(ability);
    }

    private NykthosShrineToNyx(final NykthosShrineToNyx card) {
        super(card);
    }

    @Override
    public NykthosShrineToNyx copy() {
        return new NykthosShrineToNyx(this);
    }
}

enum NykthosManaAmounts implements ManaTypeAmountProvider {
    instance;

    @Override
    public Map<ManaType, Integer> getManaAmounts(Game game, Ability source, Effect effect) {
        Map<ManaType, Integer> manaAmounts = new EnumMap<>(ManaType.class);
        if (game == null) {
            return manaAmounts;
        }
        manaAmounts.put(ManaType.WHITE, DevotionCount.W.calculate(game, source, effect));
        manaAmounts.put(ManaType.BLUE, DevotionCount.U.calculate(game, source, effect));
        manaAmounts.put(ManaType.BLACK, DevotionCount.B.calculate(game, source, effect));
        manaAmounts.put(ManaType.RED, DevotionCount.R.calculate(game, source, effect));
        manaAmounts.put(ManaType.GREEN, DevotionCount.G.calculate(game, source, effect));
        return manaAmounts;
    }
}
