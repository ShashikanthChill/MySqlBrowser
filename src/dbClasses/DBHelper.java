/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dbClasses;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 *
 * @author The_Humble_Fool
 */
public class DBHelper {

    Connection con;
    Statement st;
//    PreparedStatement pst;
    ObservableList<String> tablesList = FXCollections.observableArrayList();
    ObservableList<String> columnList = FXCollections.observableArrayList();
    HashMap<String, String> tableDataMap;
    ObservableList<String> tableDataList;
    ObservableList<HashMap<String, String>> tableDataMapList = FXCollections.observableArrayList();
    ObservableList<ObservableList<String>> tableDataListList = FXCollections.observableArrayList();
    ResultSet tableCols;
    ResultSetMetaData colsMetaData;
    private String catalogName;

    public DBHelper() {
        try {
            con = DriverManager.getConnection("jdbc:mysql://localhost:3306/", "root", "${passwd}");
            st = con.createStatement();
        } catch (SQLException ex) {
            Logger.getLogger(DBHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public ObservableList<String> getCatalogs() {
        try {
            ObservableList<String> catalogList = FXCollections.observableArrayList();
            DatabaseMetaData dbmd = con.getMetaData();
            ResultSet catalogs = dbmd.getCatalogs();
            while (catalogs.next()) {
                catalogList.add(catalogs.getString(1));
            }
            return catalogList;
        } catch (SQLException ex) {
            Logger.getLogger(DBHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public void useCatalog(String catalogName) {
        this.catalogName = catalogName;
        try {
            int execute = st.executeUpdate("use " + catalogName);
        } catch (SQLException ex) {
            Logger.getLogger(DBHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public ObservableList<String> getTables() {
        tablesList.clear();
        try {
            ResultSet tables = st.executeQuery("show tables");
            while (tables.next()) {
                tablesList.add(tables.getString(1));
            }
            return tablesList;
        } catch (SQLException ex) {
            Logger.getLogger(DBHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public ObservableList<String> getTableColumns(String tableName) {
        try {
            tableCols = st.executeQuery("desc " + tableName);
            colsMetaData = tableCols.getMetaData();
            columnList.clear();
            while (tableCols.next()) {
                columnList.add(tableCols.getString(1));
            }
            tableCols.beforeFirst();
            return columnList;
        } catch (SQLException ex) {
            Logger.getLogger(DBHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public ObservableList<HashMap<String, String>> getTableDataAsMap() {
        tableDataMapList.clear();
        if (tableCols != null & colsMetaData != null) {
            try {
                while (tableCols.next()) {
                    tableDataMap = new HashMap<>();
                    int count = 1;
                    while (count <= colsMetaData.getColumnCount()) {
                        tableDataMap.put(colsMetaData.getColumnName(count), tableCols.getString(count));
                        count++;
                    }
                    tableDataMapList.add(tableDataMap);
                }
                tableCols.close();
                tableCols = null;
                colsMetaData = null;
                return tableDataMapList;
            } catch (SQLException ex) {
                Logger.getLogger(DBHelper.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }

    public ObservableList<ObservableList<String>> getTableDataAsList() {
        tableDataListList.clear();
        if (tableCols != null & colsMetaData != null) {
            try {
                while (tableCols.next()) {
                    tableDataList = FXCollections.observableArrayList();
                    int count = 1;
                    while (count <= colsMetaData.getColumnCount()) {
                        tableDataList.add(tableCols.getString(count));
                        count++;
                    }
                    tableDataListList.add(tableDataList);
                }
                tableCols.close();
                tableCols = null;
                colsMetaData = null;
//                System.out.println("Table data list list in DBHelper: " + tableDataListList);
                return tableDataListList;
            } catch (SQLException ex) {
                Logger.getLogger(DBHelper.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }

    public ObservableList<ObservableList<String>> getTableData(String tableName, List<String> selectedParams, int rowCount) {
        try {
            StringBuilder query = new StringBuilder("select ");
            query.append(String.join(", ", selectedParams));
            query.append(" from ").append(tableName).append(" limit ").append(rowCount);
            System.out.println("query:" + query);
            ObservableList<ObservableList<String>> rows;
            try (ResultSet rset = st.executeQuery(query.toString())) {
                ResultSetMetaData rsmd = rset.getMetaData();
                rows = FXCollections.observableArrayList();
                while (rset.next()) {
                    ObservableList<String> row = FXCollections.observableArrayList();
                    int count = 1;
                    while (count <= rsmd.getColumnCount()) {
                        row.add(rset.getString(count));
                        count++;
                    }
                    rows.add(row);
                }
            }
            System.out.println("Fetched: " + rows.size() + " records.");
            return rows;
        } catch (SQLException ex) {
            Logger.getLogger(DBHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

}