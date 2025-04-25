package me.m4nst3in.m4plugins.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.m4nst3in.m4plugins.M4Pets;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;

public class ConnectionManager {
    
    private final M4Pets plugin;
    private HikariDataSource dataSource;
    
    public ConnectionManager(M4Pets plugin) {
        this.plugin = plugin;
        setupDatabase();
    }
    
    /**
     * Configura a conexão com o banco de dados
     */
    private void setupDatabase() {
        // Criar pasta de dados se não existir
        File dataFolder = new File(plugin.getDataFolder(), "data");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        
        // Obter configuração do arquivo config.yml
        String filename = plugin.getConfigManager().getMainConfig().getString("database.filename", "m4pets.db");
        
        // Caminho do arquivo do banco de dados
        String databasePath = new File(dataFolder, filename).getAbsolutePath();
        
        // Configurar HikariCP
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("org.sqlite.JDBC");
        config.setJdbcUrl("jdbc:sqlite:" + databasePath);
        
        // Configurações adicionais
        config.setMaximumPoolSize(plugin.getConfigManager().getMainConfig().getInt("database.hikari.maximum-pool-size", 10));
        config.setMinimumIdle(plugin.getConfigManager().getMainConfig().getInt("database.hikari.minimum-idle", 5));
        config.setMaxLifetime(plugin.getConfigManager().getMainConfig().getLong("database.hikari.max-lifetime", 1800000));
        config.setConnectionTimeout(plugin.getConfigManager().getMainConfig().getLong("database.hikari.connection-timeout", 30000));
        
        // Inicializar pool de conexões
        dataSource = new HikariDataSource(config);
        
        // Criar tabelas se não existirem
        createTables();
        
        plugin.getLogger().info("Conexão com banco de dados SQLite estabelecida com sucesso!");
    }
    
    /**
     * Cria as tabelas necessárias no banco de dados
     */
    private void createTables() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Tabela de pets
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS pets (" +
                "pet_id TEXT PRIMARY KEY, " +
                "owner_id TEXT NOT NULL, " +
                "name TEXT NOT NULL, " +
                "type TEXT NOT NULL, " +
                "variant TEXT NOT NULL, " +
                "level INTEGER NOT NULL DEFAULT 1, " +
                "health REAL NOT NULL, " +
                "max_health REAL NOT NULL, " +
                "dead INTEGER NOT NULL DEFAULT 0, " +
                "cosmetic TEXT" +
                ")"
            );
            
            // Tabela de dados de jogadores (para futuras estatísticas)
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS players (" +
                "player_id TEXT PRIMARY KEY, " +
                "total_pets INTEGER DEFAULT 0, " +
                "last_login LONG, " +
                "favorite_pet TEXT" +
                ")"
            );
            
            // Tabela de cosméticos adquiridos
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS cosmetics (" +
                "player_id TEXT NOT NULL, " +
                "cosmetic_id TEXT NOT NULL, " +
                "purchase_time LONG, " +
                "PRIMARY KEY (player_id, cosmetic_id)" +
                ")"
            );
            
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Erro ao criar tabelas no banco de dados", e);
        }
    }
    
    /**
     * Obtém uma conexão do pool
     */
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
    
    /**
     * Fecha todas as conexões
     */
    public void closeConnections() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}