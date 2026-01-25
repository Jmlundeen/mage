package mage.client.table;

import mage.ws.v1.view.ViewProto;

import javax.swing.table.AbstractTableModel;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class MatchesTableModel extends AbstractTableModel {

    private final String[] columnNames = new String[]{"Deck Type", "Players", "Game Type", "Rating", "Result", "Duration", "Start Time", "End Time", "Action"};
    public static final int COLUMN_DURATION = 5;
    public static final int COLUMN_START = 6;
    public static final int COLUMN_END = 7;
    public static final int COLUMN_ACTION = 8; // column the action is located (starting with 0)

    private ViewProto.MatchView[] matches = new ViewProto.MatchView[0];

    public void loadData(List<ViewProto.MatchView> matches) {
        this.matches = matches.toArray(new ViewProto.MatchView[0]);
        this.fireTableDataChanged();
    }

    MatchesTableModel() {
    }


    public String getTableAndGameInfo(int row) {
        return this.matches[row].getTableId() + ";" + (!matches[row].getGamesList().isEmpty() ? this.matches[row].getGamesList().get(0) : "null");
    }

    public String findTableAndGameInfoByRow(int row) {
        if (row >= 0 && row < this.matches.length) {
            return getTableAndGameInfo(row);
        } else {
            return null;
        }
    }

    public int findRowByTableAndGameInfo(String tableAndGame) {
        for (int i = 0; i < this.matches.length; i++) {
            String rowID = this.matches[i].getTableId() + ";" + (!this.matches[i].getGamesList().isEmpty() ? this.matches[i].getGamesList().get(0) : "null");
            if (tableAndGame.equals(rowID)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public int getRowCount() {
        return matches.length;
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public Object getValueAt(int arg0, int arg1) {
        ViewProto.MatchView match = this.matches[arg0];
        switch (arg1) {
            case 0:
                return match.getDeckType();
            case 1:
                return match.getPlayers();
            case 2:
                return match.getGameType();
            case 3:
                return match.getRated() ? TablesTableModel.RATED_VALUE_YES : TablesTableModel.RATED_VALUE_NO;
            case 4:
                return match.getResult();
            case 5:
                if (match.getEndTimeMillis() > 0 && match.getStartTimeMillis() > 0) {
                    return match.getEndTimeMillis() - match.getStartTimeMillis() + new Date().getTime();
                } else {
                    return 0L;
                }
            case 6:
                return match.getStartTimeMillis() > 0 ? new Date(match.getStartTimeMillis()) : null;
            case 7:
                return match.getEndTimeMillis() > 0 ? new Date(match.getEndTimeMillis()) : null;
            case 8:
                if (match.getIsTournament()) {
                    return "Show";
                } else if (match.getReplayAvailable()) {
                    return "Replay";
                } else {
                    return "None";
                }
            case 9:
                return match.getGamesList();
            default:
                return "";
        }
    }

    public java.util.List<UUID> getListofGames(int row) {
        java.util.List<UUID> res = new java.util.ArrayList<>();
        for (String gid : matches[row].getGamesList()) {
            if (gid != null && !gid.isEmpty()) {
                res.add(UUID.fromString(gid));
            }
        }
        return res;
    }

    public boolean isTournament(int row) {
        return matches[row].getIsTournament();
    }

    public UUID getMatchId(int row) {
        String id = matches[row].getMatchId();
        return id.isEmpty() ? null : UUID.fromString(id);
    }

    public UUID getTableId(int row) {
        String id = matches[row].getTableId();
        return id.isEmpty() ? null : UUID.fromString(id);
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
            case COLUMN_DURATION:
                return Long.class;
            case COLUMN_START:
                return Date.class;
            case COLUMN_END:
                return Date.class;
            default:
                return String.class;
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex == COLUMN_ACTION;
    }

}
