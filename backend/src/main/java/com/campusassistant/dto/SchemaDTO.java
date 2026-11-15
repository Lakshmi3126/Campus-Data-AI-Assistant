package com.campusassistant.dto;

import java.util.List;

public class SchemaDTO {
    private List<TableInfo> tables;

    public SchemaDTO() {}

    public SchemaDTO(List<TableInfo> tables) {
        this.tables = tables;
    }

    public List<TableInfo> getTables() {
        return tables;
    }

    public void setTables(List<TableInfo> tables) {
        this.tables = tables;
    }

    public static class TableInfo {
        private String tableName;
        private String description;
        private List<ColumnInfo> columns;
        private List<ForeignKeyInfo> foreignKeys;

        public TableInfo() {}

        public TableInfo(String tableName, String description, List<ColumnInfo> columns, List<ForeignKeyInfo> foreignKeys) {
            this.tableName = tableName;
            this.description = description;
            this.columns = columns;
            this.foreignKeys = foreignKeys;
        }

        public String getTableName() {
            return tableName;
        }

        public void setTableName(String tableName) {
            this.tableName = tableName;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public List<ColumnInfo> getColumns() {
            return columns;
        }

        public void setColumns(List<ColumnInfo> columns) {
            this.columns = columns;
        }

        public List<ForeignKeyInfo> getForeignKeys() {
            return foreignKeys;
        }

        public void setForeignKeys(List<ForeignKeyInfo> foreignKeys) {
            this.foreignKeys = foreignKeys;
        }
    }

    public static class ColumnInfo {
        private String columnName;
        private String dataType;
        private boolean isPrimaryKey;
        private boolean isNullable;
        private String description;

        public ColumnInfo() {}

        public ColumnInfo(String columnName, String dataType, boolean isPrimaryKey, boolean isNullable, String description) {
            this.columnName = columnName;
            this.dataType = dataType;
            this.isPrimaryKey = isPrimaryKey;
            this.isNullable = isNullable;
            this.description = description;
        }

        public String getColumnName() {
            return columnName;
        }

        public void setColumnName(String columnName) {
            this.columnName = columnName;
        }

        public String getDataType() {
            return dataType;
        }

        public void setDataType(String dataType) {
            this.dataType = dataType;
        }

        public boolean isPrimaryKey() {
            return isPrimaryKey;
        }

        public void setPrimaryKey(boolean primaryKey) {
            isPrimaryKey = primaryKey;
        }

        public boolean isNullable() {
            return isNullable;
        }

        public void setNullable(boolean nullable) {
            isNullable = nullable;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }

    public static class ForeignKeyInfo {
        private String columnName;
        private String referencedTable;
        private String referencedColumn;

        public ForeignKeyInfo() {}

        public ForeignKeyInfo(String columnName, String referencedTable, String referencedColumn) {
            this.columnName = columnName;
            this.referencedTable = referencedTable;
            this.referencedColumn = referencedColumn;
        }

        public String getColumnName() {
            return columnName;
        }

        public void setColumnName(String columnName) {
            this.columnName = columnName;
        }

        public String getReferencedTable() {
            return referencedTable;
        }

        public void setReferencedTable(String referencedTable) {
            this.referencedTable = referencedTable;
        }

        public String getReferencedColumn() {
            return referencedColumn;
        }

        public void setReferencedColumn(String referencedColumn) {
            this.referencedColumn = referencedColumn;
        }
    }
}
