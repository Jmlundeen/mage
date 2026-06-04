package mage.players;

import mage.Mana;
import mage.abilities.Ability;
import mage.abilities.costs.Cost;
import mage.abilities.effects.mana.ManaEffect;
import mage.constants.Duration;
import mage.constants.ManaType;
import mage.constants.PhaseStep;
import mage.constants.TurnPhase;
import mage.filter.Filter;
import mage.filter.FilterMana;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.game.events.GameEvent.EventType;
import mage.game.events.LoseUnspentManaEvent;
import mage.game.events.ManaEvent;
import mage.game.events.ManaPaidEvent;
import mage.game.stack.Spell;

import java.io.Serializable;
import java.util.*;

/**
 * @author BetaSteward_at_googlemail.com
 */
public class ManaPool implements Serializable {

    private final UUID playerId;

    private final List<ManaPoolItem> manaItems = new ArrayList<>();

    private boolean autoPayment; // auto payment from mana pool: true - mode is active
    private boolean autoPaymentRestricted; // auto payment from mana pool: true - if auto Payment is on, it will only pay if one kind of mana is in the pool
    private ManaType unlockedManaType; // type of mana that was selected to pay manually
    private boolean forcedToPay; // for Word of Command
    private final List<ManaPoolItem> poolBookmark = new ArrayList<>(); // mana pool bookmark for rollback purposes

    // empty mana pool effects
    private final Set<ManaType> doNotEmptyManaTypes = new HashSet<>(); // keep some colors

    public ManaPool(UUID playerId) {
        this.playerId = playerId;
        autoPayment = true;
        autoPaymentRestricted = true;
        unlockedManaType = null;
        forcedToPay = false;
    }

    protected ManaPool(final ManaPool pool) {
        this.playerId = pool.playerId;
        for (ManaPoolItem item : pool.manaItems) {
            manaItems.add(item.copy());
        }
        this.autoPayment = pool.autoPayment;
        this.autoPaymentRestricted = pool.autoPaymentRestricted;
        this.unlockedManaType = pool.unlockedManaType;
        this.forcedToPay = pool.forcedToPay;
        for (ManaPoolItem item : pool.poolBookmark) {
            poolBookmark.add(item.copy());
        }
        this.doNotEmptyManaTypes.addAll(pool.doNotEmptyManaTypes);
    }

    public int getRed() {
        return get(ManaType.RED);
    }

    public int getGreen() {
        return get(ManaType.GREEN);
    }

    public int getBlue() {
        return get(ManaType.BLUE);
    }

    public int getWhite() {
        return get(ManaType.WHITE);
    }

    public int getBlack() {
        return get(ManaType.BLACK);
    }

    /**
     * @param manaType      the mana type that should be paid
     * @param ability
     * @param filter
     * @param game
     * @param costToPay     complete costs to pay (needed to check conditional
     *                      mana)
     * @param usedManaToPay the information about what mana was paid
     * @return
     */
    public boolean pay(ManaType manaType, Ability ability, Filter filter, Game game, Cost costToPay, Mana usedManaToPay) {
        if (!isAutoPayment() && manaType != unlockedManaType) {
            // if manual payment and the needed mana type was not unlocked, nothing will be paid
            return false;
        }
        ManaType possibleAsThoughPoolManaType = null;
        if (isAutoPayment()
                && isAutoPaymentRestricted()
                && !wasManaAddedBeyondStock() // was not more mana added than at the start of casting something
                && manaType != unlockedManaType) {
            // if automatic restricted payment and there is already mana in the pool
            // and the needed mana type was not unlocked, nothing will be paid
            if (unlockedManaType != null) {
                ManaPoolItem checkItem = new ManaPoolItem();
                checkItem.add(unlockedManaType, 1);
                possibleAsThoughPoolManaType = game.getContinuousEffects().asThoughMana(manaType, checkItem, ability.getSourceId(), ability, ability.getControllerId(), game);
            }
            // Check if it's possible to use mana as thought for the unlocked manatype in the mana pool for this ability
            if (possibleAsThoughPoolManaType == null
                    || possibleAsThoughPoolManaType != unlockedManaType) {
                return false; // if it's not possible return
            }
        }

        for (ManaPoolItem mana : manaItems) {
            if (filter != null && !filter.match(mana.getSourceObject(), game)) {
                // If here, then mana source does not match the filter
                // However, alternate mana payment abilities such as convoke won't match the filter but are valid
                // So we need to do some ugly checks to allow them
                // For convoke, mana apparently comes from a spell without a mana effect, that doesn't match the ability source
                if (ability.getSourceId().equals(mana.getSourceId())
                        || !(mana.getSourceObject() instanceof Spell)
                        || ((Spell) mana.getSourceObject())
                        .getAbilities(game)
                        .stream()
                        .flatMap(a -> a.getAllEffects().stream())
                        .anyMatch(ManaEffect.class::isInstance)) {
                    continue; // if any of the above cases, not an alt mana payment ability, thus excluded by filter
                }
            }
            if (possibleAsThoughPoolManaType == null
                    && manaType != unlockedManaType
                    && isAutoPayment()
                    && isAutoPaymentRestricted()
                    && mana.count() == mana.getStock()) {
                // no mana added beyond the stock so don't auto pay this
                continue;
            }
            ManaType usableManaType = game.getContinuousEffects().asThoughMana(manaType, mana, ability.getSourceId(), ability, ability.getControllerId(), game);
            if (usableManaType == null) {
                continue;
            }
            if (!mana.conditionsApply(ability, game, mana.getSourceId(), costToPay)) {
                continue;
            }
            if (mana.get(usableManaType) > 0) {
                GameEvent event = new ManaPaidEvent(ability, mana.getSourceId(), mana.getFlag(), mana.getOriginalId(), mana.getSourceObject(), usableManaType);
                game.fireEvent(event);
                usedManaToPay.increase(usableManaType);
                mana.remove(usableManaType);
                if (mana.count() == 0) { // so no items with count 0 stay in list
                    manaItems.remove(mana);
                }
                lockManaType(); // pay only one mana if mana payment is set to manually
                return true;
            }
        }
        return false;
    }

    public int get(ManaType manaType) {
        return getMana().get(manaType);
    }

    public int getColorless() {
        return get(ManaType.COLORLESS);
    }

    public void clearEmptyManaPoolRules() {
        doNotEmptyManaTypes.clear();
    }

    public void addDoNotEmptyManaType(ManaType manaType) {
        doNotEmptyManaTypes.add(manaType);
    }

    public void init() {
        manaItems.clear();
    }

    /**
     * Checks if there is any mana that would be lost on emptying the pool.
     * Used to check whether to warn the player about mana loss on passing.
     * @return true if mana would be lost
     */
    public boolean canLoseManaOnEmpty() {
        for (ManaPoolItem item : manaItems) {
            if (extractManaToEmpty(item, null, null).count() > 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Empties the mana pool, checks replacement effects, and finally removes items from the pool.
     * If the event is replaced, pending items are added back to the pool. So, replacement effects should
     * modify the event items to be placed in the pool if replacing the event.
     * @param game current game instance
     * @param source ability that is causing the pool to empty
     * @return the total amount of mana removed from the pool
     */
    public int emptyPool(Game game, Ability source) {
        List<ManaPoolItem> pendingEmptyMana = collectPendingEmptyMana(game, source);
        if (pendingEmptyMana.isEmpty()) {
            return 0;
        }

        LoseUnspentManaEvent event = new LoseUnspentManaEvent(
                playerId,
                source,
                pendingEmptyMana
        );
        if (game.replaceEvent(event)) {
            manaItems.addAll(event.getManaItems());
            return 0;
        }
        game.fireEvent(event);
        return pendingEmptyMana.stream().mapToInt(ManaPoolItem::count).sum();
    }

    private List<ManaPoolItem> collectPendingEmptyMana(Game game, Ability source) {
        List<ManaPoolItem> result = new ArrayList<>();
        for (ManaPoolItem item : new ArrayList<>(manaItems)) {
            ManaPoolItem toEmpty = extractManaToEmpty(item, game, source);
            if (toEmpty.count() > 0) {
                ManaPoolItem eventItem;
                if (toEmpty.count() == item.count()) {
                    eventItem = item;
                } else {
                    clearMana(item, toEmpty);
                    eventItem = toEmpty.copy();
                }
                result.add(eventItem);
                manaItems.remove(eventItem);
            }
        }
        return result;
    }

    private ManaPoolItem extractManaToEmpty(ManaPoolItem item, Game game, Ability source) {
        ManaPoolItem result = item.copy();
        clearMana(result, result.copy());

        if (source == null && !shouldEmptyNow(item, game)) {
            return result;
        }

        for (ManaType manaType : ManaType.values()) {
            if (doNotEmptyManaTypes.contains(manaType) && source == null) {
                continue;
            }
            int amount = item.get(manaType);
            if (amount > 0) {
                result.add(manaType, amount);
            }
        }
        return result;
    }

    private boolean shouldEmptyNow(ManaPoolItem item, Game game) {
        if (game == null) {
            return true;
        }
        return switch (item.getDuration()) {
            case EndOfTurn -> game.getTurnPhaseType() == TurnPhase.END;
            case EndOfCombat -> game.getTurnPhaseType() == TurnPhase.COMBAT
                    && game.getTurnStepType() == PhaseStep.END_COMBAT;
            default -> true;
        };
    }

    private void clearMana(ManaPoolItem item, ManaPoolItem manaToClear) {
        for (ManaType manaType : ManaType.values()) {
            if (manaToClear.get(manaType) > 0) {
                item.clear(manaType);
            }
        }
    }

    public Mana getMana() {
        Mana m = new Mana();
        for (ManaPoolItem item : manaItems) {
            m.add(item.getMana());
        }
        return m;
    }

    public Mana getMana(FilterMana filter) {
        if (filter == null) {
            return getMana();
        }
        Mana test = getMana();
        Mana m = new Mana();
        if (filter.isBlack()) {
            m.setBlack(test.getBlack());
        }
        if (filter.isBlue()) {
            m.setBlue(test.getBlue());
        }
        if (filter.isGreen()) {
            m.setGreen(test.getGreen());
        }
        if (filter.isRed()) {
            m.setRed(test.getRed());
        }
        if (filter.isWhite()) {
            m.setWhite(test.getWhite());
        }
        if (filter.isColorless()) {
            m.setColorless(test.getColorless());
        }
        if (filter.isGeneric()) {
            m.setGeneric(test.getGeneric());
        }
        return m;
    }

    public void addMana(Mana manaToAdd, Game game, Ability source) {
        addMana(manaToAdd, game, source, false);
    }

    public void addMana(Mana manaToAdd, Game game, Ability source, boolean dontLoseUntilEOT) {
        addMana(manaToAdd, game, source, dontLoseUntilEOT ? Duration.EndOfTurn : null);
    }

    public void addMana(Mana manaToAdd, Game game, Ability source, Duration duration) {
        if (manaToAdd != null) {
            Mana mana = manaToAdd.copy();
            if (!game.replaceEvent(new ManaEvent(EventType.ADD_MANA, source.getId(), source, playerId, mana))) {
                ManaPoolItem item = new ManaPoolItem(
                        mana,
                        source.getSourceObject(game),
                        source.getOriginalId()
                );
                if (duration != null) {
                    item.setDuration(duration);
                }
                this.manaItems.add(item);

                ManaEvent manaEvent = new ManaEvent(EventType.MANA_ADDED, source.getId(), source, playerId, mana);
                manaEvent.setData(mana.toString());
                game.fireEvent(manaEvent);
            }
        }
    }

    public List<Mana> getConditionalMana() {
        List<Mana> conditionalMana = new ArrayList<>();
        for (ManaPoolItem item : manaItems) {
            if (item.isConditional()) {
                conditionalMana.add(item.getMana());
            }
        }
        return conditionalMana;
    }

    public int count() {
        int x = 0;
        for (ManaPoolItem item : manaItems) {
            x += item.count();
        }
        return x;
    }

    public ManaPool copy() {
        return new ManaPool(this);
    }

    public boolean isAutoPayment() {
        return autoPayment || forcedToPay;
    }

    public void setAutoPayment(boolean autoPayment) {
        this.autoPayment = autoPayment;
    }

    public boolean isAutoPaymentRestricted() {
        return autoPaymentRestricted || forcedToPay;
    }

    public void setAutoPaymentRestricted(boolean autoPaymentRestricted) {
        this.autoPaymentRestricted = autoPaymentRestricted;
    }

    public ManaType getUnlockedManaType() {
        return unlockedManaType;
    }

    public void unlockManaType(ManaType manaType) {
        this.unlockedManaType = manaType;
    }

    public void lockManaType() {
        this.unlockedManaType = null;
    }

    public void setStock() {
        for (ManaPoolItem mana : manaItems) {
            mana.setStock(mana.count());
        }
    }

    private boolean wasManaAddedBeyondStock() {
        for (ManaPoolItem mana : manaItems) {
            if (mana.getStock() < mana.count()) {
                return true;
            }
        }
        return false;
    }

    public boolean isEmpty() {
        return count() == 0;
    }

    public List<ManaPoolItem> getManaItems() {
        List<ManaPoolItem> itemsCopy = new ArrayList<>();
        for (ManaPoolItem manaItem : manaItems) {
            itemsCopy.add(manaItem.copy());
        }
        return itemsCopy;
    }

    public void setForcedToPay(boolean forcedToPay) {
        this.forcedToPay = forcedToPay;
    }

    public boolean isForcedToPay() {
        return forcedToPay;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public void storeMana() {
        poolBookmark.clear();
        poolBookmark.addAll(getManaItems());
    }

    public List<ManaPoolItem> getPoolBookmark() {
        List<ManaPoolItem> itemsCopy = new ArrayList<>();
        for (ManaPoolItem manaItem : poolBookmark) {
            itemsCopy.add(manaItem.copy());
        }
        return itemsCopy;
    }

    public void restoreMana(List<ManaPoolItem> manaList) {
        manaItems.clear();
        if (!manaList.isEmpty()) {
            List<ManaPoolItem> itemsCopy = new ArrayList<>();
            for (ManaPoolItem manaItem : manaList) {
                itemsCopy.add(manaItem.copy());
            }
            manaItems.addAll(itemsCopy);
        }
    }

    public int getColoredAmount(ManaType manaType) {
        return switch (manaType) {
            case BLACK -> getBlack();
            case BLUE -> getBlue();
            case GREEN -> getGreen();
            case RED -> getRed();
            case WHITE -> getWhite();
            default -> throw new IllegalArgumentException("Wrong mana type " + manaType);
        };
    }

    @Override
    public String toString() {
        return getMana().toString();
    }
}
