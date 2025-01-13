<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ page import = "java.sql.*" %>

<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Database SQL Load</title>
    </head>
    <style>
        .error {
            color: red;
        }
        pre {
            color: green;
        }
    </style>
    <body>
        <h2>Database SQL Load</h2>
        <%
            /* How to customize:
             * 1. Update the database name on dbname.
             * 2. Create the list of tables, under tablenames[].
             * 3. Create the list of table definition, under tables[].
             * 4. Create the data into the above table, under data[]. 
             * 
             * If there is any problem, it will exit at the very first error.
             */
            String dbname = "sob_grup_56";
            String schema = "ROOT";
            Class.forName("org.apache.derby.jdbc.ClientDriver");
            /* this will generate database if not exist */
            Connection con = DriverManager.getConnection("jdbc:derby://localhost:1527/" + dbname, "root", "root");
            Statement stmt = con.createStatement();
            
            /* inserting data */
            /* you have to exclude the id autogenerated from the list of columns if you have use it. */
            String data[] = new String[]{
                // Inserint usuaris
            "INSERT INTO " + schema + ".USUARI (id, password, nom, username, email) VALUES (NEXT VALUE FOR USER_GEN, 'soydios', 'Joaquim', 'Joaqui32', 'joaquim@example.com')",
            "INSERT INTO " + schema + ".USUARI (id, nom, username, email) VALUES (NEXT VALUE FOR USER_GEN, 'john', 'john_doe', 'john@example.com')",
            "INSERT INTO " + schema + ".USUARI (id, nom, username, email) VALUES (NEXT VALUE FOR USER_GEN, 'sob_john', 'sob', 'sob@example.com')",
            "INSERT INTO " + schema + ".USUARI (id, nom, username, email) VALUES (NEXT VALUE FOR USER_GEN, 'ton', 'ton_llop', 'ton@example.com')",
            "INSERT INTO " + schema + ".USUARI (id, nom, username, email) VALUES (NEXT VALUE FOR USER_GEN, 'test1_autor', 'test1', 'test@example.com')",
            "INSERT INTO " + schema + ".CREDENTIALS VALUES (NEXT VALUE FOR CREDENTIALS_GEN, 'sob', 'sob')", 
            "INSERT INTO " + schema + ".CREDENTIALS VALUES (NEXT VALUE FOR CREDENTIALS_GEN, 'ton_llop', 'llop')",
            "INSERT INTO " + schema + ".CREDENTIALS VALUES (NEXT VALUE FOR CREDENTIALS_GEN, 'test', 'test')",
            "INSERT INTO " + schema + ".CREDENTIALS VALUES (NEXT VALUE FOR CREDENTIALS_GEN, 'test1', 'test1')",
            "INSERT INTO " + schema + ".TOPIC VALUES (NEXT VALUE FOR TOPIC_GEN, 'Linux Guia')",
            "INSERT INTO " + schema + ".TOPIC VALUES (NEXT VALUE FOR TOPIC_GEN, 'Sistemes Oberts')",
            "INSERT INTO " + schema + ".TOPIC VALUES (NEXT VALUE FOR TOPIC_GEN, 'Computadors')"
            };
            for (String datum : data) {
                if (stmt.executeUpdate(datum)<=0) {
                    out.println("<span class='error'>Error inserting data: " + datum + "</span>");
                    return;
                }
                out.println("<pre> -> " + datum + "<pre>");
            }
        %>
        <button onclick="window.location='<%=request.getSession().getServletContext().getContextPath()%>'">Go home</button>
    </body>
</html>
