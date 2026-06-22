package mage.abilities.mana;

import mage.ObjectColor;
import mage.abilities.Ability;
import mage.abilities.costs.Cost;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.dynamicvalue.DynamicValue;
import mage.abilities.dynamicvalue.common.StaticValue;
import mage.abilities.effects.Effect;
import mage.abilities.effects.mana.ManaEffect;
import mage.abilities.mana.providers.ManaTypeProvider;
import mage.cards.Card;
import mage.constants.ManaType;
import mage.constants.Zone;
import mage.filter.FilterTyped;
import mage.game.Game;
import mage.players.Player;
import mage.util.ObjectQuery;

import java.util.*;

/**
 * Flexible any-color mana ability that can combine color checks from cards or
 * permanents with producible mana checks from battlefield permanents.
 * @author jmlundeen
 */
public class AnyColorAmongManaAbility extends ActivatedManaAbilityImpl {

    private AnyColorAmongManaAbility(Builder builder) {
        super(Zone.BATTLEFIELD, createEffect(builder), builder.costs.isEmpty() ? null : builder.costs.getFirst());
        builder.costs.stream().skip(1).forEach(this::addCost);
    }

    protected AnyColorAmongManaAbility(final AnyColorAmongManaAbility ability) {
        super(ability);
    }

    @Override
    public AnyColorAmongManaAbility copy() {
        return new AnyColorAmongManaAbility(this);
    }

    @Override
    public boolean definesMana(Game game) {
        return true;
    }

    public static Builder builder(FilterTyped filter, Zone... zones) {
        if (zones.length == 0) {
            return builder(filter);
        }
        return new Builder(filter, zones);
    }

    public static Builder builder(FilterTyped filter) {
        return new Builder(filter);
    }

    private static ManaEffect createEffect(Builder builder) {
        if (builder.zones.isEmpty()) {
            throw new IllegalStateException("At least one zone check must be defined");
        }
        if (builder.ruleText == null || builder.ruleText.isBlank()) {
            throw new IllegalStateException("Rule text must be defined");
        }
        return ComposedManaAbilityBuilder.builder()
                .addDynamicChoice(builder.amount, new AnyColorAmongManaTypeProvider(builder.filter, builder.zones, builder.onlyColors, builder.onlyProducibleManaTypes))
                .ruleText(builder.ruleText)
                .buildEffect();
    }

    private static void addManaTypesFromColor(Set<ManaType> manaTypes, ObjectColor color) {
        if (color.isBlack()) {
            manaTypes.add(ManaType.BLACK);
        }
        if (color.isBlue()) {
            manaTypes.add(ManaType.BLUE);
        }
        if (color.isGreen()) {
            manaTypes.add(ManaType.GREEN);
        }
        if (color.isRed()) {
            manaTypes.add(ManaType.RED);
        }
        if (color.isWhite()) {
            manaTypes.add(ManaType.WHITE);
        }
    }

    private static boolean hasAllColors(Set<ManaType> manaTypes) {
        return manaTypes.contains(ManaType.WHITE)
                && manaTypes.contains(ManaType.BLUE)
                && manaTypes.contains(ManaType.BLACK)
                && manaTypes.contains(ManaType.RED)
                && manaTypes.contains(ManaType.GREEN);
    }

    public static final class Builder {

        private final FilterTyped filter;
        private final Set<Zone> zones;
        private DynamicValue amount = StaticValue.get(1);
        private final List<Cost> costs = new ArrayList<>() {{
            add(new TapSourceCost());
        }};
        private boolean onlyColors = true;
        private boolean onlyProducibleManaTypes;
        private String ruleText;

        public Builder(FilterTyped filter, Zone... zones) {
            this(filter, Set.of(zones));
        }

        public Builder(FilterTyped filter) {
            this(filter, Set.of(Zone.BATTLEFIELD));
        }

        public Builder(FilterTyped filter, Set<Zone> zones) {
            if (filter == null) {
                throw new IllegalArgumentException("Filter can't be null");
            }
            if (zones == null || zones.isEmpty()) {
                throw new IllegalArgumentException("At least one zone must be provided");
            }
            this.filter = filter;
            this.zones = Set.copyOf(zones);
        }

        public Builder amount(DynamicValue amount) {
            this.amount = amount == null ? StaticValue.get(1) : amount;
            return this;
        }

        public Builder cost(Cost cost) {
            if (cost == null) {
                return this;
            }
            this.costs.add(cost);
            return this;
        }

        public Builder onlyColors(boolean onlyColors) {
            this.onlyColors = onlyColors;
            return this;
        }

        public Builder ruleText(String ruleText) {
            this.ruleText = ruleText;
            return this;
        }

        public Builder onlyProducibleManaTypes(boolean onlyProducibleManaTypes) {
            this.onlyProducibleManaTypes = onlyProducibleManaTypes;
            return this;
        }

        public AnyColorAmongManaAbility build() {
            return new AnyColorAmongManaAbility(this);
        }
    }

    private static class AnyColorAmongManaTypeProvider implements ManaTypeProvider {
        private final FilterTyped filter;
        private final Set<Zone> zones;
        private final boolean onlyColors;
        private final boolean onlyProducibleManaTypes;
        private transient boolean inManaTypeCalculation = false;

        public AnyColorAmongManaTypeProvider(FilterTyped filter, Set<Zone> zones, boolean onlyColors, boolean onlyProducibleManaTypes) {
            this.filter = filter;
            this.zones = zones;
            this.onlyColors = onlyColors;
            this.onlyProducibleManaTypes = onlyProducibleManaTypes;
        }

        private AnyColorAmongManaTypeProvider(final AnyColorAmongManaTypeProvider provider) {
                this(provider.filter.copy(), Set.copyOf(provider.zones), provider.onlyColors, provider.onlyProducibleManaTypes);
        }

        @Override
        public Set<ManaType> getManaTypes(Game game, Ability source, Effect effect) {
            Set<ManaType> manaTypes = EnumSet.noneOf(ManaType.class);
            if (game == null || source == null || inManaTypeCalculation) {
                return manaTypes;
            }

            inManaTypeCalculation = true;
            addManaTypes(filter, zones, manaTypes, game, source);

            if (onlyColors) {
                manaTypes.remove(ManaType.COLORLESS);
            }
            inManaTypeCalculation = false;
            return manaTypes;
        }

        @Override
        public AnyColorAmongManaTypeProvider copy() {
            return new AnyColorAmongManaTypeProvider(this);
        }

        private Set<ManaType> getProducibleManaTypes(Card card, Game game) {
            Set<ManaType> producibleManaTypes = new HashSet<>();
            for (Zone zone : zones) {
                if (hasAllColors(producibleManaTypes)) {
                    break;
                }
                for (ActivatedManaAbilityImpl ability : card.getAbilities(game).getActivatedManaAbilities(zone)) {
                    producibleManaTypes.addAll(ability.getProducableManaTypes(game));
                }
            }
            return producibleManaTypes;
        }

        private void addManaTypes(FilterTyped filter, Set<Zone> zones, Set<ManaType> manaTypes, Game game, Ability source) {
            Player controller = game.getPlayer(source.getControllerId());
            if (controller == null) {
                return;
            }

            List<Card> affectedObjects = ObjectQuery.queryCards(game, controller, source, zones, filter);
            affectedObjects.forEach(object -> {
                        if (this.onlyProducibleManaTypes) {
                            manaTypes.addAll(getProducibleManaTypes(object, game));
                        } else {
                            addManaTypesFromColor(manaTypes, object.getColor(game));
                        }
            });
        }
    }
}




