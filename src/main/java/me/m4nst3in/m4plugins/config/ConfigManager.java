package me.m4nst3in.m4plugins.config;

import me.m4nst3in.m4plugins.M4Pets;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;

public class ConfigManager {
    
    private final M4Pets plugin;
    private FileConfiguration mainConfig;
    private FileConfiguration messagesConfig;
    private FileConfiguration texturesConfig;
    
    private File mainConfigFile;
    private File messagesConfigFile;
    private File texturesConfigFile;
    
    public ConfigManager(M4Pets plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Carrega todas as configurações
     */
    public void loadConfigurations() {
        loadMainConfig();
        loadMessagesConfig();
        loadTexturesConfig();
    }
    
    /**
     * Carrega a configuração principal
     */
    public void loadMainConfig() {
        if (mainConfigFile == null) {
            mainConfigFile = new File(plugin.getDataFolder(), "config.yml");
        }
        
        if (!mainConfigFile.exists()) {
            plugin.saveResource("config.yml", false);
        }
        
        mainConfig = YamlConfiguration.loadConfiguration(mainConfigFile);
        
        // Atualizar configuração com valores padrão se necessário
        try (Reader defaultReader = new InputStreamReader(
                plugin.getResource("config.yml"), StandardCharsets.UTF_8)) {
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(defaultReader);
            
            for (String key : defaultConfig.getKeys(true)) {
                if (!mainConfig.contains(key)) {
                    mainConfig.set(key, defaultConfig.get(key));
                }
            }
            
            saveMainConfig();
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Não foi possível carregar a configuração padrão", e);
        }
    }
    
    /**
     * Carrega a configuração de mensagens
     */
    public void loadMessagesConfig() {
        if (messagesConfigFile == null) {
            messagesConfigFile = new File(plugin.getDataFolder(), "messages.yml");
        }
        
        if (!messagesConfigFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        
        messagesConfig = YamlConfiguration.loadConfiguration(messagesConfigFile);
        
        // Atualizar configuração com valores padrão se necessário
        try (Reader defaultReader = new InputStreamReader(
                plugin.getResource("messages.yml"), StandardCharsets.UTF_8)) {
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(defaultReader);
            
            for (String key : defaultConfig.getKeys(true)) {
                if (!messagesConfig.contains(key)) {
                    messagesConfig.set(key, defaultConfig.get(key));
                }
            }
            
            saveMessagesConfig();
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Não foi possível carregar a configuração de mensagens padrão", e);
        }
    }
    
    /**
     * Carrega a configuração de texturas
     */
    public void loadTexturesConfig() {
        if (texturesConfigFile == null) {
            texturesConfigFile = new File(plugin.getDataFolder(), "textures.yml");
        }
        
        if (!texturesConfigFile.exists()) {
            plugin.saveResource("textures.yml", false);
        }
        
        texturesConfig = YamlConfiguration.loadConfiguration(texturesConfigFile);
        
        // Atualizar configuração com valores padrão se necessário
        try (Reader defaultReader = new InputStreamReader(
                plugin.getResource("textures.yml"), StandardCharsets.UTF_8)) {
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(defaultReader);
            
            for (String key : defaultConfig.getKeys(true)) {
                if (!texturesConfig.contains(key)) {
                    texturesConfig.set(key, defaultConfig.get(key));
                }
            }
            
            saveTexturesConfig();
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Não foi possível carregar a configuração de texturas padrão", e);
        }
    }
    
    /**
     * Salva a configuração principal
     */
    public void saveMainConfig() {
        if (mainConfig == null || mainConfigFile == null) return;
        
        try {
            mainConfig.save(mainConfigFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Não foi possível salvar a configuração para " + mainConfigFile, e);
        }
    }
    
    /**
     * Salva a configuração de mensagens
     */
    public void saveMessagesConfig() {
        if (messagesConfig == null || messagesConfigFile == null) return;
        
        try {
            messagesConfig.save(messagesConfigFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Não foi possível salvar as mensagens para " + messagesConfigFile, e);
        }
    }
    
    /**
     * Salva a configuração de texturas
     */
    public void saveTexturesConfig() {
        if (texturesConfig == null || texturesConfigFile == null) return;
        
        try {
            texturesConfig.save(texturesConfigFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Não foi possível salvar as texturas para " + texturesConfigFile, e);
        }
    }
    
    /**
     * Recarrega todas as configurações
     */
    public void reloadConfigurations() {
        loadMainConfig();
        loadMessagesConfig();
        loadTexturesConfig();
    }
    
    /**
     * Obtém uma mensagem da configuração de mensagens
     */
    public String getMessage(String path) {
        return messagesConfig.getString("messages." + path, "Mensagem não encontrada: " + path);
    }
    
    /**
     * Getters para as configurações
     */
    public FileConfiguration getMainConfig() {
        return mainConfig;
    }
    
    public FileConfiguration getMessagesConfig() {
        return messagesConfig;
    }
    
    public FileConfiguration getTexturesConfig() {
        return texturesConfig;
    }
}