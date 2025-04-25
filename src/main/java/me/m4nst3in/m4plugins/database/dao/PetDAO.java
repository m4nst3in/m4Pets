package me.m4nst3in.m4plugins.database.dao;

import me.m4nst3in.m4plugins.database.ConnectionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PetDAO {
    
    private final ConnectionManager connectionManager;
    private final Logger logger = Logger.getLogger(PetDAO.class.getName());
    
    public PetDAO(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }
    
    /**
     * Salva ou atualiza um pet no banco de dados
     */
    public void savePet(Map<String, Object> petData) throws SQLException {
        String query = "INSERT OR REPLACE INTO pets (pet_id, owner_id, name, type, variant, level, health, max_health, dead, cosmetic) " +
                       "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, (String) petData.get("petId"));
            stmt.setString(2, (String) petData.get("ownerId"));
            stmt.setString(3, (String) petData.get("name"));
            stmt.setString(4, (String) petData.get("type"));
            stmt.setString(5, (String) petData.get("variant"));
            stmt.setInt(6, (Integer) petData.get("level"));
            stmt.setDouble(7, (Double) petData.get("health"));
            stmt.setDouble(8, (Double) petData.get("maxHealth"));
            stmt.setInt(9, (Boolean) petData.get("dead") ? 1 : 0);
            stmt.setString(10, petData.containsKey("cosmetic") ? (String) petData.get("cosmetic") : null);
            
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erro ao salvar pet no banco de dados", e);
            throw e;
        }
    }
    
    /**
     * Carrega um pet específico pelo ID
     */
    public Map<String, Object> getPet(UUID petId) throws SQLException {
        String query = "SELECT * FROM pets WHERE pet_id = ?";
        
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, petId.toString());
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractPetFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erro ao carregar pet do banco de dados", e);
            throw e;
        }
        
        return null;
    }
    
    /**
     * Carrega todos os pets de um jogador específico
     */
    public List<Map<String, Object>> getPlayerPets(UUID playerId) throws SQLException {
        String query = "SELECT * FROM pets WHERE owner_id = ?";
        List<Map<String, Object>> pets = new ArrayList<>();
        
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, playerId.toString());
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    pets.add(extractPetFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erro ao carregar pets do jogador", e);
            throw e;
        }
        
        return pets;
    }
    
    /**
     * Carrega todos os pets de todos os jogadores
     */
    public Map<UUID, List<Map<String, Object>>> getAllPets() throws SQLException {
        String query = "SELECT * FROM pets";
        Map<UUID, List<Map<String, Object>>> allPets = new HashMap<>();
        
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Map<String, Object> pet = extractPetFromResultSet(rs);
                UUID ownerId = UUID.fromString((String) pet.get("ownerId"));
                
                allPets.putIfAbsent(ownerId, new ArrayList<>());
                allPets.get(ownerId).add(pet);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erro ao carregar todos os pets", e);
            throw e;
        }
        
        return allPets;
    }
    
    /**
     * Deleta um pet do banco de dados
     */
    public void deletePet(UUID petId) throws SQLException {
        String query = "DELETE FROM pets WHERE pet_id = ?";
        
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, petId.toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erro ao deletar pet", e);
            throw e;
        }
    }
    
    /**
     * Extrai dados de um pet do ResultSet
     */
    private Map<String, Object> extractPetFromResultSet(ResultSet rs) throws SQLException {
        Map<String, Object> pet = new HashMap<>();
        
        pet.put("petId", rs.getString("pet_id"));
        pet.put("ownerId", rs.getString("owner_id"));
        pet.put("name", rs.getString("name"));
        pet.put("type", rs.getString("type"));
        pet.put("variant", rs.getString("variant"));
        pet.put("level", rs.getInt("level"));
        pet.put("health", rs.getDouble("health"));
        pet.put("maxHealth", rs.getDouble("max_health"));
        pet.put("dead", rs.getInt("dead") == 1);
        
        String cosmetic = rs.getString("cosmetic");
        if (cosmetic != null) {
            pet.put("cosmetic", cosmetic);
        }
        
        return pet;
    }
}