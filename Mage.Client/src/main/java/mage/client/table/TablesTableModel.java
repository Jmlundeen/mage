package mage.client.table;

import mage.client.SessionHandler;
import mage.components.table.TableModelWithTooltip;
import mage.constants.SkillLevel;
import mage.remote.MageRemoteException;
import mage.ws.v1.view.ViewProto;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.util.Collection;
import java.util.Date;
import java.util.UUID;

public class TablesTableModel extends AbstractTableModel implements TableModelWithTooltip {

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

    private ViewProto.TableView[] tables = new ViewProto.TableView[0];

    TablesTableModel() {
    }

    public void loadData(Collection<ViewProto.TableView> tables) throws MageRemoteException {
        this.tables = tables.toArray(new ViewProto.TableView[0]);
        this.fireTableDataChanged();
    }

    public String getTableAndGameInfo(int row) {
        String tableId = tables[row].getTableId();
        String gameId = (tables[row].getGamesCount() > 0 ? tables[row].getGames(0) : "null");
        return tableId + ";" + (gameId.isEmpty() ? "null" : gameId);
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
            String rowID = this.tables[i].getTableId() + ";" + (this.tables[i].getGamesCount() > 0 ? this.tables[i].getGames(0) : "null");
            if (tableAndGame.equals(rowID)) {
                return i;
            }
        }
        return -1;
    }

    public String getSkillLevelAsCode(SkillLevel skill, boolean asRegExp) {
        String res;
        switch (skill) {
            case BEGINNER:
                res = "*";
                break;
            case CASUAL:
                res = "**";
                break;
            case SERIOUS:
                res = "***";
                break;
            default:
                res = "";
                break;
        }

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
        ViewProto.TableView table = tables[rowIndex];
        switch (columnIndex) {
            case 0:
                return table.getIsTournament() ? tourneyIcon : matchIcon;
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
                return table.getIsPasswordProtected() ? PASSWORD_VALUE_YES : "";
            case 9:
                return table.getCreateTimeMillis() > 0 ? new Date(table.getCreateTimeMillis()) : null;
            case 10:
                return table.getSkillLevel();
            case 11:
                return table.getRated() ? RATED_VALUE_YES : RATED_VALUE_NO;
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
                        if (table.getIsTournament()) {
                            return "Show";
                        }
                    case DUELING:
                        if (table.getIsTournament()) {
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
                return table.getIsTournament();
            case 16:
                if (table.getGamesCount() > 0) {
                    String gid = table.getGames(0);
                    if (!gid.isEmpty()) {
                        return UUID.fromString(gid);
                    }
                }
                return null;
            case 17:
                if (!table.getTableId().isEmpty()) {
                    return UUID.fromString(table.getTableId());
                }
                return null;
        }
        return "";
    }

    @Override
    public String getTooltipAt(int rowIndex, int columnIndex) {
        Object res;
        switch (columnIndex) {
            case COLUMN_INFO:
                res = tables[rowIndex].getAdditionalInfoFull();
                break;
            default:
                res = this.getValueAt(rowIndex, columnIndex);
                break;
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
        switch (columnIndex) {
            case COLUMN_ICON:
                return Icon.class;
            case COLUMN_SKILL:
                return SkillLevel.class;
            case COLUMN_CREATED:
                return Date.class;
            default:
                return String.class;
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex == ACTION_COLUMN;
    }

}
