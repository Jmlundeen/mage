package mage.client.table;

import mage.client.SessionHandler;
import mage.components.table.TableModelWithTooltip;
import mage.constants.SkillLevel;
import mage.remote.MageRemoteException;
import mage.view.TableView;
import org.apache.log4j.Logger;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.util.Collection;
import java.util.Date;

public class TablesTableModel extends AbstractTableModel implements TableModelWithTooltip {

    static final Logger logger = Logger.getLogger(TablesTableModel.class);
    // icons with tostring for tables hints
    final ImageIcon tourneyIcon = new ImageIcon(getClass().getResource("/tables/tourney_icon.png")) {
        @Override
        public String toString() {
            return "Tourney";
        }
    };
    final ImageIcon matchIcon = new ImageIcon(getClass().getResource("/tables/match_icon.png")) {
        @Override
        public String toString() {
            return "Match";
        }
    };

    public static final int COLUMN_ICON = 0;
    public static final int COLUMN_DECK_TYPE = 1; // column the deck type is located (starting with 0) Start string is used to check for Limited
    public static final int COLUMN_NAME = 2;
    public static final int COLUMN_SEATS = 3;
    public static final int COLUMN_OWNER = 4;
    public static final int COLUMN_GAME_TYPE = 5;
    public static final int COLUMN_INFO = 6;
    public static final int COLUMN_STATUS = 7;
    public static final int COLUMN_PASSWORD = 8;
    public static final int COLUMN_CREATED = 9;
    public static final int COLUMN_SKILL = 10;
    public static final int COLUMN_RATING = 11;
    public static final int COLUMN_QUIT_RATIO = 12;
    public static final int COLUMN_MINIMUM_RATING = 13;
    public static final int ACTION_COLUMN = 14; // column the action is located (starting with 0)

    public static final String RATED_VALUE_YES = "YES";
    public static final String RATED_VALUE_NO = "";

    public static final String PASSWORD_VALUE_YES = "YES";

    private final String[] columnNames = new String[]{"M/T", "Deck Type", "Name", "Seats", "Owner / Players", "Game Type", "Info", "Status", "Password", "Created / Started", "Skill Level", "Rated", "Quit %", "Min Rating", "Action"};

    private TableView[] tables = new TableView[0];

    TablesTableModel() {
    }

    public void loadData(Collection<TableView> tables) throws MageRemoteException {
        this.tables = tables.toArray(new TableView[0]);
        logger.debug("Tables loaded: " + this.tables.length);
        this.fireTableDataChanged();
    }

    public String getTableAndGameInfo(int row) {
        String tableAndGame = this.tables[row].getTableId().toString() + ";" + (!tables[row].getGames().isEmpty() ? tables[row].getGames().getFirst().toString() : "null");
        logger.debug("getTableAndGameInfo: " + tableAndGame);
        return tableAndGame;
    }

    public String findTableAndGameInfoByRow(int row) {
        if (row >= 0 && row < this.tables.length) {
            return getTableAndGameInfo(row);
        } else {
            return null;
        }
    }

    public int findRowByTableAndGameInfo(String tableAndGame) {
        for (int i = 0; i < this.tables.length; i++) {
            String rowID = this.tables[i].getTableId().toString() + ";" + (!this.tables[i].getGames().isEmpty() ? this.tables[i].getGames().getFirst().toString() : "null");
            if (tableAndGame.equals(rowID)) {
                return i;
            }
        }
        return -1;
    }

    public String getSkillLevelAsCode(SkillLevel skill, boolean asRegExp) {
        String res = switch (skill) {
            case BEGINNER -> "*";
            case CASUAL -> "**";
            case SERIOUS -> "***";
        };

        // regexp format for search table rows
        if (asRegExp) {
            res = String.format("^%s$", res.replace("*", "\\*"));
        }

        return res;
    }

    @Override
    public int getRowCount() {
        return tables.length;
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        TableView table = tables[rowIndex];
        switch (columnIndex) {
            case 0:
                return table.isTournament() ? tourneyIcon : matchIcon;
            case 1:
                return table.getDeckType();
            case 2:
                return table.getTableName();
            case 3:
                return table.getSeatsInfo();
            case 4:
                return table.getControllerName();
            case 5:
                return table.getGameType();
            case 6:
                return table.getAdditionalInfoShort();
            case 7:
                return table.getTableStateText();
            case 8:
                return table.isPassworded() ? PASSWORD_VALUE_YES : "";
            case 9:
                return table.getCreateTime(); // use cell render, not format here
            case 10:
                return this.getSkillLevelAsCode(table.getSkillLevel(), false);
            case 11:
                return table.isRated() ? RATED_VALUE_YES : RATED_VALUE_NO;
            case 12:
                return table.getQuitRatio();
            case 13:
                return table.getMinimumRating();
            case 14:
                switch (table.getTableState()) {
                    case WAITING:
                        String ownerWaiting = table.getControllerName();
                        if (SessionHandler.getSession() != null && ownerWaiting.equals(SessionHandler.getUserName())) {
                            return "";
                        }
                        return "Join";
                    case CONSTRUCTING:
                    case DRAFTING:
                        if (table.isTournament()) {
                            return "Show";
                        }
                    case DUELING:
                        if (table.isTournament()) {
                            return "Show";
                        } else {
                            String ownerDueling = table.getControllerName();
                            if (SessionHandler.getSession() != null && ownerDueling.equals(SessionHandler.getUserName())) {
                                return "";
                            }
                            if (table.getSpectatorsAllowed()) {
                                return "Watch";
                            }
                            return "";
                        }
                    default:
                        return "";
                }
            case 15:
                return table.isTournament();
            case 16:
                if (!table.getGames().isEmpty()) {
                    return table.getGames().getFirst();
                }
                return null;
            case 17:
                return table.getTableId();
        }
        return "";
    }

    @Override
    public String getTooltipAt(int rowIndex, int columnIndex) {
        Object res;
        if (columnIndex == COLUMN_INFO) {
            res = tables[rowIndex].getAdditionalInfoFull();
        } else {
            res = this.getValueAt(rowIndex, columnIndex);
        }
        return res.toString();
    }

    @Override
    public String getColumnName(int columnIndex) {
        String colName = "";

        if (columnIndex <= getColumnCount()) {
            colName = columnNames[columnIndex];
        }

        return colName;
    }

    @Override
    public Class getColumnClass(int columnIndex) {
        return switch (columnIndex) {
            case COLUMN_ICON -> Icon.class;
            case COLUMN_SKILL -> SkillLevel.class;
            case COLUMN_CREATED -> Date.class;
            default -> String.class;
        };
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex == ACTION_COLUMN;
    }

}
