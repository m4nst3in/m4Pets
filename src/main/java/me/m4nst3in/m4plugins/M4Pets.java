package me.m4nst3in.m4plugins;

import me.m4nst3in.m4plugins.commands.PetCommand;
import me.m4nst3in.m4plugins.config.ConfigManager;
import me.m4nst3in.m4plugins.database.ConnectionManager;
import me.m4nst3in.m4plugins.gui.GUIManager;
import me.m4nst3in.m4plugins.listeners.GUIListener;
import me.m4nst3in.m4plugins.listeners.PetListener;
import me.m4nst3in.m4plugins.pets.PetManager;
import me.m4nst3in.m4plugins.util.HologramUtil;
import me.m4nst3in.m4plugins.util.TextUtil;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Classe principal do plugin M4Pets
 * @author m4nst3in
 * @version 1.0.0
 */
public class M4Pets extends JavaPlugin {
    
    private static M4Pets instance;
    private ConfigManager configManager;
    private ConnectionManager connectionManager;
    private PetManager petManager;
    private GUIManager guiManager;
    private HologramUtil hologramUtil;
    private Economy economy;
    private Logger logger;
    private boolean decentHologramsEnabled = false;
    
    @Override
    public void onEnable() {
        instance = this;
        this.logger = getLogger();
        
        logger.info("Iniciando M4Pets v" + getDescription().getVersion() + " por " + getDescription().getAuthors().get(0));
        
        // Carregando configurações
        this.configManager = new ConfigManager(this);
        this.configManager.loadConfigurations();
        
        // Verificando dependências
        if (!setupEconomy()) {
            logger.severe("Vault não encontrado ou não há plugin de economia! Desativando plugin...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        // Verificando DecentHolograms
        if (Bukkit.getPluginManager().getPlugin("DecentHolograms") != null) {
            logger.info("DecentHolograms encontrado! Habilitando suporte a hologramas...");
            decentHologramsEnabled = true;
            this.hologramUtil = new HologramUtil(this);
        } else {
            logger.warning("DecentHolograms não encontrado! O recurso de hologramas será desabilitado.");
        }
        
        try {
            // Inicializando banco de dados
            this.connectionManager = new ConnectionManager(this);
            
            // Inicializando gerenciadores
            this.petManager = new PetManager(this);
            this.guiManager = new GUIManager(this);
            
            // Registrando comandos
            getCommand("pets").setExecutor(new PetCommand(this));
            getCommand("pets").setTabCompleter(new PetCommand(this));
            getCommand("warrior").setExecutor(new me.m4nst3in.m4plugins.commands.WarriorCommand(this));
            getCommand("warrior").setTabCompleter(new me.m4nst3in.m4plugins.commands.WarriorCommand(this));
            
            // Registrando listeners
            getServer().getPluginManager().registerEvents(new GUIListener(this), this);
            getServer().getPluginManager().registerEvents(new PetListener(this), this);
            
            // Inicializando tarefas agendadas
            initScheduledTasks();
            
            logger.info("M4Pets inicializado com sucesso!");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Erro ao inicializar o plugin!", e);
            getServer().getPluginManager().disablePlugin(this);
        }
    }
    
    @Override
    public void onDisable() {
        // Salvar dados antes de desligar
        if (petManager != null) {
            petManager.despawnAllPets();
            petManager.saveAllPetData();
        }
        
        // Remover hologramas
        if (hologramUtil != null) {
            hologramUtil.removeAllHolograms();
        }
        
        // Fechar conexões do banco de dados
        if (connectionManager != null) {
            connectionManager.closeConnections();
        }
        
        logger.info("M4Pets desativado com sucesso!");
    }
    
    /**
     * Configura a economia usando Vault
     * @return true se configurada com sucesso
     */
    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return economy != null;
    }
    
    /**
     * Inicializa tarefas agendadas
     */
    private void initScheduledTasks() {
        // Task para salvar dados periodicamente
        int saveInterval = configManager.getMainConfig().getInt("general.save-interval", 5) * 1200; // Convertendo para ticks (minutos * 60 * 20)
        if (saveInterval > 0) {
            getServer().getScheduler().runTaskTimerAsynchronously(this, () -> {
                if (configManager.getMainConfig().getBoolean("general.debug", false)) {
                    logger.info("Salvando dados de pets...");
                }
                petManager.saveAllPetData();
                if (configManager.getMainConfig().getBoolean("general.debug", false)) {
                    logger.info("Dados salvos com sucesso!");
                }
            }, saveInterval, saveInterval);
        }

        int nameUpdateInterval = configManager.getMainConfig().getInt("pets.global.name-update-interval", 20);
        getServer().getScheduler().runTaskTimer(this, () -> {
            petManager.updateAllPetDisplayNames();
        }, nameUpdateInterval, nameUpdateInterval);
        
        // Task para verificar distância máxima dos pets
        int checkInterval = 100; // 5 segundos
        getServer().getScheduler().runTaskTimer(this, () -> {
            int maxDistance = configManager.getMainConfig().getInt("pets.global.max-distance", 30);
            if (maxDistance > 0) {
                petManager.checkPetsDistance(maxDistance);
            }
        }, checkInterval, checkInterval);
        
        // Task para proteger pets contra fogo solar
        getServer().getScheduler().runTaskTimer(this, () -> {
            petManager.protectPetsFromSunlight();
        }, 20L, 40L); // A cada 2 segundos
    }
    
    /**
     * Obtém a instância do plugin
     * @return Instância do plugin
     */
    public static M4Pets getInstance() {
        return instance;
    }
    
    /**
     * Obtém o gerenciador de configurações
     * @return Gerenciador de configurações
     */
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    /**
     * Obtém o gerenciador de conexões com banco de dados
     * @return Gerenciador de conexões
     */
    public ConnectionManager getConnectionManager() {
        return connectionManager;
    }
    
    /**
     * Obtém o gerenciador de pets
     * @return Gerenciador de pets
     */
    public PetManager getPetManager() {
        return petManager;
    }
    
    /**
     * Obtém o gerenciador de interfaces gráficas
     * @return Gerenciador de GUIs
     */
    public GUIManager getGuiManager() {
        return guiManager;
    }
    
    /**
     * Obtém o gerenciador de hologramas
     * @return Gerenciador de hologramas
     */
    public HologramUtil getHologramUtil() {
        return hologramUtil;
    }
    
    /**
     * Obtém a economia
     * @return Economia do Vault
     */
    public Economy getEconomy() {
        return economy;
    }
    
    /**
     * Verifica se o suporte a hologramas está ativado
     * @return true se o suporte a hologramas estiver ativado
     */
    public boolean isDecentHologramsEnabled() {
        return decentHologramsEnabled;
    }
    
    /**
     * Formata uma mensagem com o prefixo do plugin
     * @param message Mensagem a ser formatada
     * @return Mensagem formatada com o prefixo
     */
    public String formatMessage(String message) {
        return TextUtil.color(configManager.getMainConfig().getString("general.prefix") + message);
    }
}