package me.m4nst3in.m4plugins.pets.abstractpets;

import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import me.m4nst3in.m4plugins.M4Pets;
import me.m4nst3in.m4plugins.pets.PetType;
import me.m4nst3in.m4plugins.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.*;

public abstract class AbstractPet {
    
    /**
     * Teleports the pet to its owner's location if the owner is online.
     */
    public void teleportToOwner() {
        Player owner = Bukkit.getPlayer(ownerId);
        if (owner != null && owner.isOnline() && entity != null && !entity.isDead()) {
            entity.teleport(owner.getLocation());
        }
    }
    
    protected final M4Pets plugin;
    protected final UUID ownerId;
    protected UUID petId; // ID único do pet
    protected String petName; // Nome personalizado do pet
    protected Entity entity; // Entidade do pet no mundo
    protected PetType type; // Tipo do pet
    protected String variant; // Variante do pet (cor, etc.)
    protected int level; // Nível do pet (1-5)
    protected double health; // Vida atual do pet
    protected double maxHealth; // Vida máxima do pet
    protected boolean dead; // Se o pet está morto
    protected boolean spawned; // Se o pet está invocado
    protected long lastSpawnTime; // Último momento em que foi invocado
    protected String cosmeticParticle; // Partícula cosmética ativa
    // Removido: protected Hologram hologram; // Holograma acima do pet
    
    public AbstractPet(M4Pets plugin, UUID ownerId, String petName, PetType type, String variant) {
        this.plugin = plugin;
        this.ownerId = ownerId;
        this.petId = UUID.randomUUID();
        this.petName = petName;
        this.type = type;
        this.variant = variant;
        this.level = 1;
        this.dead = false;
        this.spawned = false;
        this.cosmeticParticle = null;
        
        // Inicializar com valores base da configuração
        ConfigurationSection petConfig = getPetConfigSection();
        if (petConfig != null) {
            this.maxHealth = petConfig.getDouble("base-health", 20);
            this.health = this.maxHealth;
        } else {
            this.maxHealth = 20;
            this.health = 20;
        }
    }
    
    /**
     * Invocar o pet no mundo
     * @param player Jogador dono do pet
     * @return true se o pet foi invocado com sucesso
     */
    public boolean spawn(Player player) {
        if (dead) {
            player.sendMessage(plugin.formatMessage(plugin.getConfigManager().getMessage("general.pet-dead")));
            return false;
        }
        
        // Verificar cooldown
        int cooldown = plugin.getConfigManager().getMainConfig().getInt("pets.global.spawn-cooldown", 10);
        long currentTime = System.currentTimeMillis();
        if ((currentTime - lastSpawnTime) / 1000 < cooldown) {
            long remainingTime = cooldown - ((currentTime - lastSpawnTime) / 1000);
            player.sendMessage(plugin.formatMessage(plugin.getConfigManager().getMessage("general.pet-cooldown")
                    .replace("%time%", String.valueOf(remainingTime))));
            return false;
        }
        
        // Verificar se já está invocado
        if (spawned && entity != null && !entity.isDead()) {
            despawn();
        }
        
        // Invocar entidade
        entity = spawnEntityType(player.getLocation());
        
        if (entity != null) {
            spawned = true;
            lastSpawnTime = currentTime;
            
            // Customizar entidade com nome personalizado incluindo vida
            customizeEntity();
            
            // Não mais cria hologramas separados
            // Removido: if (plugin.isDecentHologramsEnabled()) {
            //     createHologram();
            // }
            
            player.sendMessage(plugin.formatMessage(plugin.getConfigManager().getMessage("general.pet-spawned")
                    .replace("%pet_name%", petName)));
            return true;
        }
        
        return false;
    }
    
    /**
     * Remove o pet do mundo
     */
    public void despawn() {
        if (spawned && entity != null && !entity.isDead()) {
            // Não mais precisa remover hologramas
            // Removido: if (hologram != null && plugin.isDecentHologramsEnabled()) {
            //     DHAPI.removeHologram(hologram.getName());
            //     hologram = null;
            // }
            
            // Remover entidade
            entity.remove();
            entity = null;
            spawned = false;
        }
    }
    
    /**
     * Gerar o nome completo do pet com informações de saúde e nível
     * @return Nome formatado do pet
     */
    protected String getFormattedPetName() {
        // Verificar se devemos mostrar a barra de vida
        boolean showHealthBar = plugin.getConfigManager().getMainConfig().getBoolean("pets.global.show-health-bar", true);
        boolean showLevel = plugin.getConfigManager().getMainConfig().getBoolean("pets.global.show-level", true);
        
        StringBuilder nameBuilder = new StringBuilder();
        
        // Adicionar prefixo de nível se configurado
        if (showLevel) {
            nameBuilder.append("&8[&e").append(level).append("&8] ");
        }
        
        // Adicionar nome do pet
        nameBuilder.append("&a").append(petName);
        
        // Adicionar barra de vida se configurado
        if (showHealthBar) {
            nameBuilder.append(" &8(&c");
            nameBuilder.append((int) health).append("&8/&c").append((int) maxHealth);
            nameBuilder.append("&8)");
        }
        
        return TextUtil.color(nameBuilder.toString());
    }
    
    /**
     * Atualiza o nome visível da entidade
     */
    public void updateEntityName() {
        if (spawned && entity != null && !entity.isDead()) {
            entity.setCustomName(getFormattedPetName());
            entity.setCustomNameVisible(true);
        }
    }
    
    // Métodos removidos relacionados ao holograma
    // Removido: createHologram()
    // Removido: updateHologram()
    
    /**
     * Melhora o nível do pet
     * @param player Jogador dono do pet
     * @return true se o upgrade foi bem sucedido
     */
    public boolean upgrade(Player player) {
        int maxLevel = plugin.getConfigManager().getMainConfig().getInt("pets.global.max-level", 5);
        
        if (level >= maxLevel) {
            player.sendMessage(plugin.formatMessage(plugin.getConfigManager().getMessage("general.max-level-reached")));
            return false;
        }
        
        // Calcular custo do upgrade (25% do valor do pet * nível atual)
        ConfigurationSection petConfig = getPetConfigSection();
        if (petConfig == null) return false;
        
        double petCost = petConfig.getDouble("cost", 1000);
        double upgradeCost = petCost * 0.25 * level;
        
        // Verificar se o jogador tem dinheiro suficiente
        if (plugin.getEconomy().getBalance(player) < upgradeCost) {
            player.sendMessage(plugin.formatMessage(plugin.getConfigManager().getMessage("general.not-enough-money")
                    .replace("%price%", String.format("%.2f", upgradeCost))));
            return false;
        }
        
        // Cobrar o jogador
        plugin.getEconomy().withdrawPlayer(player, upgradeCost);
        
        // Melhorar o pet
        level++;
        
        // Atualizar atributos baseados no nível
        updateAttributes();
        
        // Atualizar nome da entidade em vez do holograma
        if (spawned && entity != null) {
            updateEntityName();
        }
        
        player.sendMessage(plugin.formatMessage(plugin.getConfigManager().getMessage("general.upgrade-success")
                .replace("%pet_name%", petName)
                .replace("%level%", String.valueOf(level))));
        
        return true;
    }
    
    /**
     * Ressuscita o pet se ele estiver morto
     * @param player Jogador dono do pet
     * @return true se ressuscitou com sucesso
     */
    public boolean resurrect(Player player) {
        if (!dead) return false;
        
        // Calcular custo da ressurreição (25% do valor do pet)
        ConfigurationSection petConfig = getPetConfigSection();
        if (petConfig == null) return false;
        
        double petCost = petConfig.getDouble("cost", 1000);
        int resurrectPercent = plugin.getConfigManager().getMainConfig().getInt("economy.resurrect-cost-percent", 25);
        double resurrectCost = petCost * (resurrectPercent / 100.0);
        
        // Verificar se o jogador tem dinheiro suficiente
        if (plugin.getEconomy().getBalance(player) < resurrectCost) {
            player.sendMessage(plugin.formatMessage(plugin.getConfigManager().getMessage("general.not-enough-money")
                    .replace("%price%", String.format("%.2f", resurrectCost))));
            return false;
        }
        
        // Cobrar o jogador
        plugin.getEconomy().withdrawPlayer(player, resurrectCost);
        
        // Ressuscitar o pet
        this.dead = false;
        this.health = this.maxHealth;
        
        player.sendMessage(plugin.formatMessage(plugin.getConfigManager().getMessage("general.pet-resurrected")
                .replace("%pet_name%", petName)
                .replace("%price%", String.format("%.2f", resurrectCost))));
        
        return true;
    }
    
    /**
     * Aplicar dano ao pet
     * @param damage Quantidade de dano
     * @return true se o pet morreu com esse dano
     */
    public boolean damage(double damage) {
        if (dead) return false;
        
        health -= damage;
        
        // Atualizar nome da entidade em vez do holograma
        if (spawned && entity != null) {
            updateEntityName();
        }
        
        if (health <= 0) {
            health = 0;
            dead = true;
            despawn();
            return true;
        }
        
        return false;
    }
    
    /**
     * Renomeia o pet
     * @param newName Novo nome para o pet
     */
    public void rename(String newName) {
        this.petName = newName;
        
        // Atualizar nome da entidade
        if (spawned && entity != null) {
            updateEntityName();
        }
    }
    
    /**
     * Aplicar um cosmético de partícula ao pet
     * @param particleType Tipo de partícula
     */
    public void setParticleEffect(String particleType) {
        this.cosmeticParticle = particleType;
    }
    
    /**
     * Atualiza os atributos do pet baseado no nível atual
     */
    protected void updateAttributes() {
        ConfigurationSection petConfig = getPetConfigSection();
        if (petConfig == null) return;
        
        // Obter valores base
        double baseHealth = petConfig.getDouble("base-health", 20);
        double healthIncrease = petConfig.getDouble("health-increase-per-level", 5);
        
        // Calcular novo valor de vida máxima
        double newMaxHealth = baseHealth + (healthIncrease * (level - 1));
        
        // Atualizar valores
        this.maxHealth = newMaxHealth;
        
        // Se a vida atual for menor que a nova vida máxima, aumentar proporcionalmente
        if (this.health < this.maxHealth) {
            double healthRatio = this.health / (this.maxHealth - healthIncrease);
            this.health = healthRatio * this.maxHealth;
        } else {
            this.health = this.maxHealth;
        }
        
        // Atualizar nome da entidade
        if (spawned && entity != null) {
            updateEntityName();
        }
    }
    
    /**
     * Obtém a seção de configuração específica para este tipo de pet
     */
    protected ConfigurationSection getPetConfigSection() {
        String category = type.getConfigCategory();
        String petKey = type.name().toLowerCase();
        
        return plugin.getConfigManager().getMainConfig().getConfigurationSection("pets." + category + "." + petKey);
    }
    
    /**
     * Verifica se o pet tem a habilidade especial de nível 5
     */
    public boolean hasLevel5Ability() {
        return level >= 5;
    }
    
    /**
     * Obtém a descrição da habilidade de nível 5
     */
    public String getLevel5AbilityDescription() {
        ConfigurationSection petConfig = getPetConfigSection();
        if (petConfig == null) return "Nenhuma habilidade especial";
        
        return petConfig.getString("level5-ability", "Nenhuma habilidade especial");
    }
    
    // Métodos abstratos que devem ser implementados por subclasses
    
    /**
     * Cria a entidade no mundo
     * @param location Localização onde spawn o pet
     * @return Entidade criada
     */
    protected abstract Entity spawnEntityType(Location location);
    
    /**
     * Personaliza a entidade (nome, atributos, etc.)
     * NOTA: As subclasses devem chamar updateEntityName() em vez de definir o CustomName diretamente
     */
    protected abstract void customizeEntity();
    
    // Getters e setters
    
    public UUID getOwnerId() {
        return ownerId;
    }
    
    public UUID getPetId() {
        return petId;
    }
    
    public String getPetName() {
        return petName;
    }
    
    public Entity getEntity() {
        return entity;
    }
    
    public PetType getType() {
        return type;
    }
    
    public String getVariant() {
        return variant;
    }
    
    public void setVariant(String variant) {
        this.variant = variant;
        
        // Atualizar aparência da entidade se estiver spawned
        if (spawned && entity != null) {
            customizeEntity();
        }
    }
    
    public int getLevel() {
        return level;
    }
    
    public double getHealth() {
        return health;
    }
    
    public double getMaxHealth() {
        return maxHealth;
    }
    
    public boolean isDead() {
        return dead;
    }
    
    public boolean isSpawned() {
        return spawned;
    }
    
    public String getCosmeticParticle() {
        return cosmeticParticle;
    }
    
    /**
     * Serializa o pet para armazenamento
     * @return Mapa com os dados serializados
     */
    public Map<String, Object> serialize() {
        Map<String, Object> data = new HashMap<>();
        data.put("petId", petId.toString());
        data.put("ownerId", ownerId.toString());
        data.put("name", petName);
        data.put("type", type.name());
        data.put("variant", variant);
        data.put("level", level);
        data.put("health", health);
        data.put("maxHealth", maxHealth);
        data.put("dead", dead);
        if (cosmeticParticle != null) {
            data.put("cosmetic", cosmeticParticle);
        }
        return data;
    }
}