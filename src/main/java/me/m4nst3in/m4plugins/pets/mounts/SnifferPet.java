package me.m4nst3in.m4plugins.pets.mounts;

import me.m4nst3in.m4plugins.M4Pets;
import me.m4nst3in.m4plugins.pets.PetType;
import me.m4nst3in.m4plugins.pets.abstractpets.MountPet;
import me.m4nst3in.m4plugins.util.TextUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sniffer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class SnifferPet extends MountPet {

    private long lastFindTime = 0;
    private static final Map<UUID, BukkitRunnable> findingTasks = new HashMap<>();
    
    // Itens raros que o Sniffer pode encontrar
    private static final List<Material> RARE_ITEMS = Arrays.asList(
        Material.DIAMOND, Material.EMERALD, Material.LAPIS_LAZULI, Material.REDSTONE, 
        Material.COAL, Material.IRON_INGOT, Material.GOLD_INGOT, Material.COPPER_INGOT,
        Material.TORCHFLOWER_SEEDS, Material.PITCHER_POD, Material.AMETHYST_SHARD,
        Material.QUARTZ, Material.GLOWSTONE_DUST, Material.BLAZE_POWDER, Material.ENDER_PEARL,
        Material.SLIME_BALL, Material.PHANTOM_MEMBRANE
    );

    public SnifferPet(M4Pets plugin, UUID ownerId, String petName, String variant) {
        super(plugin, ownerId, petName, PetType.SNIFFER, variant);
    }

    @Override
    protected Entity spawnEntityType(Location location) {
        Entity entity = location.getWorld().spawnEntity(location, EntityType.SNIFFER);
        return entity;
    }

    @Override
    protected void customizeEntity() {
        if (entity instanceof Sniffer) {
            Sniffer sniffer = (Sniffer) entity;
            
            // Definir nome customizado
            sniffer.setCustomName(TextUtil.color("&a" + petName));
            sniffer.setCustomNameVisible(true);
            
            // Tornar adulto
            sniffer.setAdult();
            
            // Definir atributos
            if (sniffer.getAttribute(Attribute.MAX_HEALTH) != null) {
                sniffer.getAttribute(Attribute.MAX_HEALTH).setBaseValue(maxHealth);
                sniffer.setHealth(health);
            }
            
            if (sniffer.getAttribute(Attribute.MOVEMENT_SPEED) != null) {
                sniffer.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(speed);
            }
        }
    }

    @Override
    protected void performLevel5AbilityWhileMounted(Player player) {
        // Habilidade de nível 5: Encontra itens raros enquanto passeia
        if (entity instanceof Sniffer && hasLevel5Ability()) {
            long currentTime = System.currentTimeMillis();
            
            // Verificar se já pode encontrar outro item (3 minutos)
            if ((currentTime - lastFindTime) >= 180000) {
                // Se não houver task agendada para este pet
                if (!findingTasks.containsKey(petId)) {
                    BukkitRunnable task = new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (!isSpawned() || entity == null || entity.isDead() || !isMounted()) {
                                cancel();
                                findingTasks.remove(petId);
                                return;
                            }
                            
                            // Escolher um item aleatório da lista
                            Material item = RARE_ITEMS.get(new Random().nextInt(RARE_ITEMS.size()));
                            int amount = new Random().nextInt(3) + 1; // Entre 1 e 3
                            
                            // Dar o item ao jogador
                            player.getInventory().addItem(new ItemStack(item, amount));
                            player.sendMessage(plugin.formatMessage("&aSeu pet &e" + petName + " &aacaba de encontrar &e" + amount + "x " + formatItemName(item) + "&a!"));
                            lastFindTime = System.currentTimeMillis();
                            
                            findingTasks.remove(petId);
                        }
                        
                        // Formata o nome do item para exibição
                        private String formatItemName(Material material) {
                            String name = material.name().toLowerCase().replace('_', ' ');
                            StringBuilder result = new StringBuilder();
                            
                            for (String word : name.split("\\s")) {
                                if (word.length() > 0) {
                                    result.append(Character.toUpperCase(word.charAt(0)))
                                          .append(word.substring(1))
                                          .append(" ");
                                }
                            }
                            
                            return result.toString().trim();
                        }
                    };
                    
                    // Agendar a tarefa e armazená-la (entre 30 e 60 segundos após montar)
                    int delay = new Random().nextInt(600) + 600; // Entre 30 e 60 segundos (600-1200 ticks)
                    task.runTaskLater(plugin, delay);
                    findingTasks.put(petId, task);
                }
            }
        }
    }
    
    @Override
    public void despawn() {
        // Cancelar qualquer tarefa pendente
        if (findingTasks.containsKey(petId)) {
            findingTasks.get(petId).cancel();
            findingTasks.remove(petId);
        }
        super.despawn();
    }
}