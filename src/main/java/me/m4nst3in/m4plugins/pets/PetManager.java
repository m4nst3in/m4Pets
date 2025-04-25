package me.m4nst3in.m4plugins.pets;

import me.m4nst3in.m4plugins.M4Pets;
import me.m4nst3in.m4plugins.database.dao.PetDAO;
import me.m4nst3in.m4plugins.pets.abstractpets.AbstractPet;
import me.m4nst3in.m4plugins.pets.abstractpets.MountPet;
import me.m4nst3in.m4plugins.pets.mounts.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class PetManager {
    
    private final M4Pets plugin;
    private final PetDAO petDAO;
    private final Map<UUID, Map<UUID, AbstractPet>> playerPets; // playerUUID -> (petUUID -> Pet)
    private final Map<UUID, AbstractPet> activePets; // petUUID -> Pet (pets ativos no mundo)
    
    public PetManager(M4Pets plugin) {
        this.plugin = plugin;
        this.petDAO = new PetDAO(plugin.getConnectionManager());
        this.playerPets = new ConcurrentHashMap<>();
        this.activePets = new ConcurrentHashMap<>();
        
        // Carregar dados dos pets salvos no banco de dados
        loadAllPetData();
        
        // Agendar tarefas relacionadas aos pets (partículas, etc)
        scheduleParticleEffects();
    }
    
    /**
     * Carrega todos os dados de pets do banco de dados
     */
    private void loadAllPetData() {
        plugin.getLogger().info("Carregando dados de pets do banco de dados...");
        
        // Executar de forma assíncrona para não travar o servidor
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                // Obter todos os dados de pets do banco de dados
                Map<UUID, List<Map<String, Object>>> allPetsData = petDAO.getAllPets();
                
                // Processar os dados e criar instâncias de pet
                for (Map.Entry<UUID, List<Map<String, Object>>> entry : allPetsData.entrySet()) {
                    UUID playerUUID = entry.getKey();
                    List<Map<String, Object>> petsData = entry.getValue();
                    
                    // Criar mapa de pets para este jogador se não existir
                    playerPets.putIfAbsent(playerUUID, new ConcurrentHashMap<>());
                    
                    // Criar instâncias de pet para cada pet do jogador
                    for (Map<String, Object> petData : petsData) {
                        try {
                            // Criar pet com base nos dados
                            AbstractPet pet = createPetFromData(petData);
                            if (pet != null) {
                                playerPets.get(playerUUID).put(pet.getPetId(), pet);
                            }
                        } catch (Exception e) {
                            plugin.getLogger().log(Level.SEVERE, "Erro ao carregar pet: " + petData, e);
                        }
                    }
                }
                
                plugin.getLogger().info("Dados de pets carregados com sucesso! Total de jogadores com pets: " + playerPets.size());
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Erro ao carregar dados de pets", e);
            }
        });
    }
    
    /**
     * Cria uma instância de pet baseada nos dados do banco de dados
     */
    private AbstractPet createPetFromData(Map<String, Object> petData) {
        try {
            UUID petId = UUID.fromString((String) petData.get("petId"));
            UUID ownerId = UUID.fromString((String) petData.get("ownerId"));
            String name = (String) petData.get("name");
            PetType type = PetType.valueOf((String) petData.get("type"));
            String variant = (String) petData.get("variant");
            int level = ((Number) petData.get("level")).intValue();
            double health = ((Number) petData.get("health")).doubleValue();
            double maxHealth = ((Number) petData.get("maxHealth")).doubleValue();
            boolean dead = (boolean) petData.get("dead");
            String cosmetic = petData.containsKey("cosmetic") ? (String) petData.get("cosmetic") : null;
            
            // Criar a instância de pet baseada no tipo
            AbstractPet pet = createNewPet(ownerId, name, type, variant);
            if (pet != null) {
                // Refletir o UUID salvo
                java.lang.reflect.Field idField = AbstractPet.class.getDeclaredField("petId");
                idField.setAccessible(true);
                idField.set(pet, petId);
                
                // Definir outros atributos salvos
                java.lang.reflect.Field levelField = AbstractPet.class.getDeclaredField("level");
                levelField.setAccessible(true);
                levelField.set(pet, level);
                
                java.lang.reflect.Field healthField = AbstractPet.class.getDeclaredField("health");
                healthField.setAccessible(true);
                healthField.set(pet, health);
                
                java.lang.reflect.Field maxHealthField = AbstractPet.class.getDeclaredField("maxHealth");
                maxHealthField.setAccessible(true);
                maxHealthField.set(pet, maxHealth);
                
                java.lang.reflect.Field deadField = AbstractPet.class.getDeclaredField("dead");
                deadField.setAccessible(true);
                deadField.set(pet, dead);
                
                if (cosmetic != null) {
                    pet.setParticleEffect(cosmetic);
                }
            }
            
            return pet;
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Erro ao criar pet a partir dos dados", e);
            return null;
        }
    }
    
    /**
     * Agenda o processamento de efeitos de partículas para pets
     */
    private void scheduleParticleEffects() {
        // Task para processar efeitos visuais dos pets (partículas)
        new BukkitRunnable() {
            @Override
            public void run() {
                for (AbstractPet pet : activePets.values()) {
                    if (pet.isSpawned() && pet.getEntity() != null && !pet.getEntity().isDead()) {
                        String particleType = pet.getCosmeticParticle();
                        if (particleType != null) {
                            spawnParticleEffect(pet, particleType);
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 10L, 10L); // A cada 10 ticks (0.5 segundo)
    }
    
    /**
     * Gera efeitos de partículas ao redor do pet
     */
    private void spawnParticleEffect(AbstractPet pet, String particleType) {
        if (pet.getEntity() == null) return;
        
        Location loc = pet.getEntity().getLocation();
        
        try {
            Particle particle = Particle.valueOf(particleType);
            
            // Padrão circular de partículas
            for (int i = 0; i < 8; i++) {
                double angle = 2 * Math.PI * i / 8;
                double x = Math.cos(angle) * 0.5;
                double z = Math.sin(angle) * 0.5;
                loc.getWorld().spawnParticle(
                    particle,
                    loc.getX() + x,
                    loc.getY() + 0.5,
                    loc.getZ() + z,
                    1, 0, 0, 0, 0
                );
            }
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Tipo de partícula inválido: " + particleType);
        }
    }
    
    /**
     * Atualiza os hologramas de todos os pets ativos
     */
    public void updateAllPetHolograms() {
        if (!plugin.isDecentHologramsEnabled()) return;
        
        for (AbstractPet pet : activePets.values()) {
            if (pet.isSpawned() && pet.getEntity() != null && !pet.getEntity().isDead()) {
                pet.updateHologram();
            }
        }
    }
    
    /**
     * Cria um novo pet e o adiciona à lista do jogador
     */
    public AbstractPet createPet(Player player, PetType type, String variant) {
        // Criar o pet
        String defaultName = plugin.getConfigManager().getMainConfig()
                .getString("pets." + type.getConfigCategory() + "." + type.name().toLowerCase() + ".name", "Pet");
        
        AbstractPet pet = createNewPet(player.getUniqueId(), defaultName + " de " + player.getName(), type, variant);
        
        if (pet != null) {
            // Adicionar à coleção de pets do jogador
            UUID playerUUID = player.getUniqueId();
            playerPets.putIfAbsent(playerUUID, new ConcurrentHashMap<>());
            playerPets.get(playerUUID).put(pet.getPetId(), pet);
            
            // Salvar no banco de dados
            savePet(pet);
        }
        
        return pet;
    }
    
    /**
     * Cria uma nova instância de pet baseada no tipo
     */
    private AbstractPet createNewPet(UUID ownerId, String name, PetType type, String variant) {
        switch (type) {
            case PIG:
                return new PigPet(plugin, ownerId, name, variant);
            case HORSE:
                return new HorsePet(plugin, ownerId, name, variant);
            case DONKEY:
                return new DonkeyPet(plugin, ownerId, name, variant);
            case SHEEP:
                return new SheepPet(plugin, ownerId, name, variant);
            case COW:
                return new CowPet(plugin, ownerId, name, variant);
            case SNIFFER:
                return new SnifferPet(plugin, ownerId, name, variant);
            // Outros tipos serão implementados no futuro
            default:
                return null;
        }
    }
    
    /**
     * Salva os dados de um pet específico no banco de dados
     */
    public void savePet(AbstractPet pet) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                petDAO.savePet(pet.serialize());
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Erro ao salvar pet: " + pet.getPetId(), e);
            }
        });
    }
    
    /**
     * Salva todos os pets no banco de dados
     */
    public void saveAllPetData() {
        plugin.getLogger().info("Salvando dados de pets no banco de dados...");
        
        // Salvar cada pet de forma assíncrona
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            for (Map<UUID, AbstractPet> playerPetsMap : playerPets.values()) {
                for (AbstractPet pet : playerPetsMap.values()) {
                    try {
                        petDAO.savePet(pet.serialize());
                    } catch (Exception e) {
                        plugin.getLogger().log(Level.SEVERE, "Erro ao salvar pet: " + pet.getPetId(), e);
                    }
                }
            }
            plugin.getLogger().info("Dados de pets salvos com sucesso!");
        });
    }
    
    /**
     * Remove um pet do sistema e do banco de dados
     */
    public void removePet(Player player, AbstractPet pet) {
        // Despawnar o pet se estiver ativo
        if (pet.isSpawned()) {
            pet.despawn();
            activePets.remove(pet.getPetId());
        }
        
        // Remover das coleções
        UUID playerUUID = player.getUniqueId();
        if (playerPets.containsKey(playerUUID)) {
            playerPets.get(playerUUID).remove(pet.getPetId());
        }
        
        // Remover do banco de dados
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                petDAO.deletePet(pet.getPetId());
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Erro ao deletar pet: " + pet.getPetId(), e);
            }
        });
    }
    
    /**
     * Invocar um pet para o jogador
     */
    public boolean spawnPet(Player player, AbstractPet pet) {
        // Despawnar pet atual se houver
        UUID playerUUID = player.getUniqueId();
        for (AbstractPet activePet : new ArrayList<>(activePets.values())) {
            if (activePet.getOwnerId().equals(playerUUID)) {
                activePet.despawn();
                activePets.remove(activePet.getPetId());
            }
        }
        
        // Invocar o novo pet
        if (pet.spawn(player)) {
            activePets.put(pet.getPetId(), pet);
            return true;
        }
        
        return false;
    }
    
    /**
     * Despawnar todos os pets ativos
     */
    public void despawnAllPets() {
        for (AbstractPet pet : new ArrayList<>(activePets.values())) {
            pet.despawn();
        }
        activePets.clear();
    }
    
    /**
     * Obter lista de pets do jogador
     */
    public Collection<AbstractPet> getPlayerPets(UUID playerUUID) {
        if (playerPets.containsKey(playerUUID)) {
            return playerPets.get(playerUUID).values();
        }
        return Collections.emptyList();
    }
    
    /**
     * Obter um pet específico do jogador pelo UUID do pet
     */
    public AbstractPet getPlayerPet(UUID playerUUID, UUID petUUID) {
        if (playerPets.containsKey(playerUUID) && playerPets.get(playerUUID).containsKey(petUUID)) {
            return playerPets.get(playerUUID).get(petUUID);
        }
        return null;
    }
    
    /**
     * Obter um pet específico do jogador pelo nome do pet
     */
    public AbstractPet getPlayerPetByName(UUID playerUUID, String petName) {
        if (playerPets.containsKey(playerUUID)) {
            for (AbstractPet pet : playerPets.get(playerUUID).values()) {
                if (pet.getPetName().equalsIgnoreCase(petName)) {
                    return pet;
                }
            }
        }
        return null;
    }
    
    /**
     * Obter o pet ativo de um jogador específico
     */
    public AbstractPet getActivePlayerPet(UUID playerUUID) {
        for (AbstractPet pet : activePets.values()) {
            if (pet.getOwnerId().equals(playerUUID)) {
                return pet;
            }
        }
        return null;
    }
    
    /**
     * Verificar se o jogador já possui um pet do tipo especificado
     */
    public boolean playerHasPetType(UUID playerUUID, PetType type) {
        if (playerPets.containsKey(playerUUID)) {
            for (AbstractPet pet : playerPets.get(playerUUID).values()) {
                if (pet.getType() == type) {
                    return true;
                }
            }
        }
        return false;
    }

    public Collection<AbstractPet> getAllActivePets() {
        return activePets.values();
    }
    
    /**
     * Checks all active pets' distance from their owners and teleports them back if too far
     * @param maxDistance The maximum allowed distance in blocks
     */
    public void checkPetsDistance(int maxDistance) {
        for (AbstractPet pet : activePets.values()) {
            if (pet.isSpawned() && pet.getEntity() != null && !pet.getEntity().isDead()) {
                Player owner = Bukkit.getPlayer(pet.getOwnerId());
                if (owner != null && owner.isOnline()) {
                    double distance = pet.getEntity().getLocation().distance(owner.getLocation());
                    if (distance > maxDistance) {
                        pet.teleportToOwner();
                    }
                }
            }
        }
    }
    
    /**
     * Montar em um pet de montaria
     */
    public boolean mountPet(Player player, AbstractPet pet) {
        if (pet instanceof MountPet) {
            return ((MountPet) pet).mount(player);
        }
        return false;
    }
}