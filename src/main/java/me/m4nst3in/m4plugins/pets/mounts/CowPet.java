package me.m4nst3in.m4plugins.pets.mounts;

import me.m4nst3in.m4plugins.M4Pets;
import me.m4nst3in.m4plugins.pets.PetType;
import me.m4nst3in.m4plugins.pets.abstractpets.MountPet;
import me.m4nst3in.m4plugins.util.TextUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Cow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CowPet extends MountPet {

    private long lastMilkTime = 0;
    private static final Map<UUID, BukkitRunnable> milkTasks = new HashMap<>();
    
    public CowPet(M4Pets plugin, UUID ownerId, String petName, String variant) {
        super(plugin, ownerId, petName, PetType.COW, variant);
    }

    @Override
    protected Entity spawnEntityType(Location location) {
        Entity entity = location.getWorld().spawnEntity(location, EntityType.COW);
        return entity;
    }

    @Override
    protected void customizeEntity() {
        if (entity instanceof Cow) {
            Cow cow = (Cow) entity;
            
            // Definir nome customizado
            cow.setCustomName(TextUtil.color("&a" + petName));
            cow.setCustomNameVisible(true);
            
            // Tornar adulta
            cow.setAdult();
            
            // Definir atributos
            if (cow.getAttribute(Attribute.MAX_HEALTH) != null) {
                cow.getAttribute(Attribute.MAX_HEALTH).setBaseValue(maxHealth);
                cow.setHealth(health);
            }
            
            if (cow.getAttribute(Attribute.MOVEMENT_SPEED) != null) {
                cow.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(speed);
            }
        }
    }

    @Override
    protected void performLevel5AbilityWhileMounted(Player player) {
        // Habilidade de nível 5: Fornece leite a cada 5 minutos
        if (entity instanceof Cow && hasLevel5Ability()) {
            long currentTime = System.currentTimeMillis();
            
            // Verificar se já pode dar leite novamente (5 minutos)
            if ((currentTime - lastMilkTime) >= 300000) {
                // Se não houver task agendada para este pet
                if (!milkTasks.containsKey(petId)) {
                    BukkitRunnable task = new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (!isSpawned() || entity == null || entity.isDead() || !isMounted()) {
                                cancel();
                                milkTasks.remove(petId);
                                return;
                            }
                            
                            // Dar um balde de leite ao jogador
                            player.getInventory().addItem(new ItemStack(Material.MILK_BUCKET));
                            player.sendMessage(plugin.formatMessage("&aSeu pet &e" + petName + " &aacaba de te dar um balde de leite!"));
                            lastMilkTime = System.currentTimeMillis();
                            
                            milkTasks.remove(petId);
                        }
                    };
                    
                    // Agendar a tarefa e armazená-la
                    task.runTaskLater(plugin, 100L); // 5 segundos depois de montado
                    milkTasks.put(petId, task);
                }
            }
        }
    }
    
    @Override
    public void despawn() {
        // Cancelar qualquer tarefa pendente
        if (milkTasks.containsKey(petId)) {
            milkTasks.get(petId).cancel();
            milkTasks.remove(petId);
        }
        super.despawn();
    }
}