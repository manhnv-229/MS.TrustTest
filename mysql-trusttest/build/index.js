#!/usr/bin/env node
/** ------------------------------------------
 * Mục đích: MCP Server cho MySQL database MS.TrustTest
 * Cung cấp tools để thực thi queries, lấy thông tin tables và database schema
 * @author NVMANH with Cline
 * @created 15/11/2025 13:19
 */
import { Server } from "@modelcontextprotocol/sdk/server/index.js";
import { StdioServerTransport } from "@modelcontextprotocol/sdk/server/stdio.js";
import { CallToolRequestSchema, ListResourcesRequestSchema, ListToolsRequestSchema, ReadResourceRequestSchema, } from "@modelcontextprotocol/sdk/types.js";
import mysql from "mysql2/promise";
/** ------------------------------------------
 * Mục đích: Cấu hình kết nối MySQL
 * @author NVMANH with Cline
 * @created 15/11/2025 13:19
 */
const DB_CONFIG = {
    host: "104.199.231.104",
    port: 3306,
    user: "nvmanh",
    password: "!M@nh1989",
    database: "MS.TrustTest",
};
let connection = null;
/** ------------------------------------------
 * Mục đích: Tạo kết nối MySQL
 * @returns Connection instance
 * @author NVMANH with Cline
 * @created 15/11/2025 13:19
 */
async function getConnection() {
    if (!connection) {
        connection = await mysql.createConnection(DB_CONFIG);
    }
    return connection;
}
/** ------------------------------------------
 * Mục đích: Khởi tạo và cấu hình MCP Server
 * @author NVMANH with Cline
 * @created 15/11/2025 13:19
 */
const server = new Server({
    name: "mysql-trusttest",
    version: "1.0.0",
}, {
    capabilities: {
        resources: {},
        tools: {},
    },
});
/** ------------------------------------------
 * Mục đích: Handler cho list tools request
 * @author NVMANH with Cline
 * @created 15/11/2025 13:19
 */
server.setRequestHandler(ListToolsRequestSchema, async () => {
    return {
        tools: [
            {
                name: "execute_query",
                description: "Thực thi SQL query trên MS.TrustTest database",
                inputSchema: {
                    type: "object",
                    properties: {
                        query: {
                            type: "string",
                            description: "SQL query cần thực thi (SELECT, INSERT, UPDATE, DELETE)",
                        },
                        params: {
                            type: "array",
                            description: "Parameters cho prepared statement (optional)",
                            items: {
                                type: "string",
                            },
                        },
                    },
                    required: ["query"],
                },
            },
            {
                name: "get_table_info",
                description: "Lấy thông tin chi tiết về table (structure, indexes, constraints)",
                inputSchema: {
                    type: "object",
                    properties: {
                        tableName: {
                            type: "string",
                            description: "Tên table cần lấy thông tin",
                        },
                    },
                    required: ["tableName"],
                },
            },
            {
                name: "describe_database",
                description: "Mô tả tổng quan về database schema và relationships",
                inputSchema: {
                    type: "object",
                    properties: {},
                    additionalProperties: false,
                },
            },
        ],
    };
});
/** ------------------------------------------
 * Mục đích: Handler cho call tool request
 * @author NVMANH with Cline
 * @created 15/11/2025 13:19
 */
server.setRequestHandler(CallToolRequestSchema, async (request) => {
    const { name, arguments: args } = request.params;
    try {
        const conn = await getConnection();
        switch (name) {
            case "execute_query": {
                const { query, params = [] } = args;
                const [rows, fields] = await conn.execute(query, params);
                return {
                    content: [
                        {
                            type: "text",
                            text: JSON.stringify({
                                success: true,
                                query,
                                params,
                                rowCount: Array.isArray(rows) ? rows.length : 0,
                                data: rows,
                                fields: fields?.map((f) => ({
                                    name: f.name,
                                    type: f.type,
                                })),
                            }, null, 2),
                        },
                    ],
                };
            }
            case "get_table_info": {
                const { tableName } = args;
                // Lấy structure
                const [columns] = await conn.query(`DESCRIBE ${tableName}`);
                // Lấy indexes
                const [indexes] = await conn.query(`SHOW INDEXES FROM ${tableName}`);
                // Lấy create table statement
                const [createTable] = await conn.query(`SHOW CREATE TABLE ${tableName}`);
                return {
                    content: [
                        {
                            type: "text",
                            text: JSON.stringify({
                                tableName,
                                columns,
                                indexes,
                                createStatement: createTable[0]["Create Table"],
                            }, null, 2),
                        },
                    ],
                };
            }
            case "describe_database": {
                // Lấy danh sách tables
                const [tables] = await conn.query(`SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = ?`, [DB_CONFIG.database]);
                // Lấy danh sách views
                const [views] = await conn.query(`SELECT TABLE_NAME FROM INFORMATION_SCHEMA.VIEWS WHERE TABLE_SCHEMA = ?`, [DB_CONFIG.database]);
                // Lấy danh sách stored procedures
                const [procedures] = await conn.query(`SELECT * FROM INFORMATION_SCHEMA.ROUTINES WHERE ROUTINE_SCHEMA = ? AND ROUTINE_TYPE = 'PROCEDURE'`, [DB_CONFIG.database]);
                return {
                    content: [
                        {
                            type: "text",
                            text: JSON.stringify({
                                database: DB_CONFIG.database,
                                tables: tables.length,
                                views: views.length,
                                procedures: procedures.length,
                                tableList: tables,
                                viewList: views,
                                procedureList: procedures,
                                analyzed_at: new Date().toISOString(),
                            }, null, 2),
                        },
                    ],
                };
            }
            default:
                throw new Error(`Unknown tool: ${name}`);
        }
    }
    catch (error) {
        return {
            content: [
                {
                    type: "text",
                    text: JSON.stringify({
                        error: error.message,
                        stack: error.stack,
                    }, null, 2),
                },
            ],
            isError: true,
        };
    }
});
/** ------------------------------------------
 * Mục đích: Handler cho list resources request
 * @author NVMANH with Cline
 * @created 15/11/2025 13:19
 */
server.setRequestHandler(ListResourcesRequestSchema, async () => {
    const conn = await getConnection();
    // Lấy danh sách tables
    const [tables] = await conn.query(`SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = ? AND TABLE_TYPE = 'BASE TABLE'`, [DB_CONFIG.database]);
    // Lấy danh sách views
    const [views] = await conn.query(`SELECT TABLE_NAME FROM INFORMATION_SCHEMA.VIEWS WHERE TABLE_SCHEMA = ?`, [DB_CONFIG.database]);
    // Lấy danh sách procedures
    const [procedures] = await conn.query(`SELECT ROUTINE_NAME FROM INFORMATION_SCHEMA.ROUTINES WHERE ROUTINE_SCHEMA = ? AND ROUTINE_TYPE = 'PROCEDURE'`, [DB_CONFIG.database]);
    const resources = [
        {
            uri: `mysql://tables`,
            name: "Database Tables",
            description: `Danh sách tất cả tables trong ${DB_CONFIG.database} database`,
            mimeType: "application/json",
        },
        {
            uri: `mysql://views`,
            name: "Database Views",
            description: `Danh sách tất cả views trong ${DB_CONFIG.database} database`,
            mimeType: "application/json",
        },
        {
            uri: `mysql://procedures`,
            name: "Stored Procedures",
            description: `Danh sách tất cả stored procedures trong ${DB_CONFIG.database} database`,
            mimeType: "application/json",
        },
    ];
    // Thêm resources cho từng table
    for (const table of tables) {
        const tableName = table.TABLE_NAME;
        resources.push({
            uri: `mysql://table/${tableName}`,
            name: `Table Structure và Data: ${tableName}`,
            description: `Thông tin cấu trúc và sample data của table ${tableName}`,
            mimeType: "application/json",
        });
        resources.push({
            uri: `mysql://table/${tableName}/schema`,
            name: `Table Schema: ${tableName}`,
            description: `Chi tiết cấu trúc columns và constraints của table ${tableName}`,
            mimeType: "application/json",
        });
    }
    return { resources };
});
/** ------------------------------------------
 * Mục đích: Handler cho read resource request
 * @author NVMANH with Cline
 * @created 15/11/2025 13:19
 */
server.setRequestHandler(ReadResourceRequestSchema, async (request) => {
    const { uri } = request.params;
    const conn = await getConnection();
    if (uri === "mysql://tables") {
        const [tables] = await conn.query(`SELECT TABLE_NAME, TABLE_ROWS, CREATE_TIME, UPDATE_TIME 
       FROM INFORMATION_SCHEMA.TABLES 
       WHERE TABLE_SCHEMA = ? AND TABLE_TYPE = 'BASE TABLE'`, [DB_CONFIG.database]);
        return {
            contents: [
                {
                    uri,
                    mimeType: "application/json",
                    text: JSON.stringify(tables, null, 2),
                },
            ],
        };
    }
    if (uri === "mysql://views") {
        const [views] = await conn.query(`SELECT TABLE_NAME, VIEW_DEFINITION 
       FROM INFORMATION_SCHEMA.VIEWS 
       WHERE TABLE_SCHEMA = ?`, [DB_CONFIG.database]);
        return {
            contents: [
                {
                    uri,
                    mimeType: "application/json",
                    text: JSON.stringify(views, null, 2),
                },
            ],
        };
    }
    if (uri === "mysql://procedures") {
        const [procedures] = await conn.query(`SELECT ROUTINE_NAME, ROUTINE_DEFINITION, CREATED, LAST_ALTERED 
       FROM INFORMATION_SCHEMA.ROUTINES 
       WHERE ROUTINE_SCHEMA = ? AND ROUTINE_TYPE = 'PROCEDURE'`, [DB_CONFIG.database]);
        return {
            contents: [
                {
                    uri,
                    mimeType: "application/json",
                    text: JSON.stringify(procedures, null, 2),
                },
            ],
        };
    }
    const tableMatch = uri.match(/^mysql:\/\/table\/([^/]+)$/);
    if (tableMatch) {
        const tableName = tableMatch[1];
        const [structure] = await conn.query(`DESCRIBE ${tableName}`);
        const [sampleData] = await conn.query(`SELECT * FROM ${tableName} LIMIT 10`);
        return {
            contents: [
                {
                    uri,
                    mimeType: "application/json",
                    text: JSON.stringify({
                        tableName,
                        structure,
                        sampleData,
                    }, null, 2),
                },
            ],
        };
    }
    const schemaMatch = uri.match(/^mysql:\/\/table\/([^/]+)\/schema$/);
    if (schemaMatch) {
        const tableName = schemaMatch[1];
        const [columns] = await conn.query(`DESCRIBE ${tableName}`);
        const [indexes] = await conn.query(`SHOW INDEXES FROM ${tableName}`);
        const [createTable] = await conn.query(`SHOW CREATE TABLE ${tableName}`);
        return {
            contents: [
                {
                    uri,
                    mimeType: "application/json",
                    text: JSON.stringify({
                        tableName,
                        columns,
                        indexes,
                        createStatement: createTable[0]["Create Table"],
                    }, null, 2),
                },
            ],
        };
    }
    throw new Error(`Unknown resource: ${uri}`);
});
/** ------------------------------------------
 * Mục đích: Khởi động MCP server
 * @author NVMANH with Cline
 * @created 15/11/2025 13:19
 */
async function main() {
    const transport = new StdioServerTransport();
    await server.connect(transport);
    console.error("MySQL TrustTest MCP Server running on stdio");
}
main().catch((error) => {
    console.error("Server error:", error);
    process.exit(1);
});
//# sourceMappingURL=index.js.map