package com.ruoyi.framework.config;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Read-only startup check for employee-register schema compatibility.
 * Does not auto-migrate; logs clear guidance when code/DB versions diverge.
 */
@Component
@Order(20)
public class DbSchemaCompatibilityChecker implements ApplicationRunner
{
    private static final Logger log = LoggerFactory.getLogger(DbSchemaCompatibilityChecker.class);

    private static final List<String> REQUIRED_USER_COLUMNS = Arrays.asList(
            "allow_admin_login",
            "allow_mini_login",
            "audit_status",
            "register_source",
            "register_invite_id",
            "audit_by",
            "audit_time",
            "audit_remark",
            "dispatchable");

    private final DataSource dataSource;

    public DbSchemaCompatibilityChecker(@Qualifier("dynamicDataSource") DataSource dataSource)
    {
        this.dataSource = dataSource;
    }

    @Override
    public void run(ApplicationArguments args)
    {
        try (Connection conn = dataSource.getConnection())
        {
            String catalog = conn.getCatalog();
            List<String> missing = new ArrayList<>();
            for (String col : REQUIRED_USER_COLUMNS)
            {
                if (!columnExists(conn, catalog, "sys_user", col))
                {
                    missing.add("sys_user." + col);
                }
            }
            boolean inviteOk = tableExists(conn, catalog, "sys_dept_register_invite");
            if (!inviteOk)
            {
                missing.add("table sys_dept_register_invite");
            }

            if (missing.isEmpty())
            {
                log.info("DB schema compatibility OK (database={})", catalog);
                return;
            }

            log.error("============================================================");
            log.error("Database schema is incompatible with current application code.");
            log.error("Connected database: {}", catalog);
            log.error("Missing objects: {}", missing);
            log.error("Apply migration: backend/sql/upgrade_employee_register_invite.sql");
            log.error("Backup first. Rollback file: backend/sql/rollback_employee_register_invite.sql");
            log.error("Login and employee register will fail until migration is applied.");
            log.error("============================================================");
        }
        catch (Exception e)
        {
            log.error("Failed to verify database schema compatibility on startup", e);
        }
    }

    private boolean columnExists(Connection conn, String schema, String table, String column) throws Exception
    {
        String sql = "SELECT 1 FROM information_schema.COLUMNS "
                + "WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ? AND COLUMN_NAME = ? LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql))
        {
            ps.setString(1, schema);
            ps.setString(2, table);
            ps.setString(3, column);
            try (ResultSet rs = ps.executeQuery())
            {
                return rs.next();
            }
        }
    }

    private boolean tableExists(Connection conn, String schema, String table) throws Exception
    {
        String sql = "SELECT 1 FROM information_schema.TABLES "
                + "WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ? LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql))
        {
            ps.setString(1, schema);
            ps.setString(2, table);
            try (ResultSet rs = ps.executeQuery())
            {
                return rs.next();
            }
        }
    }
}
