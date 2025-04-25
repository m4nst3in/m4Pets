package me.m4nst3in.m4plugins.pets.mounts;

import me.m4nst3in.m4plugins.M4Pets;
import me.m4nst3in.m4plugins.pets.PetType;
import me.m4nst3in.m4plugins.pets.abstractpets.MountPet;
import me.m4nst3in.m4plugins.util.TextUtil;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Donkey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class DonkeyPet extends MountPet {

    private boolean inventoryAccessible = false;

    public DonkeyPet(M4Pets plugin, UUID ownerId, String petName, String variant) {
        super(plugin, ownerId, petName, PetType.DONKEY, variant);
    }

    @Override
    protected Entity spawnEntityType(Location location) {
        Entity entity = location.getWorld().spawnEntity(location, EntityType.DONKEY);
        return entity;
    }

    @Override
    protected void customizeEntity() {
        if (entity instanceof Donkey) {
            Donkey donkey = (Donkey) entity;
            
            // Definir nome customizado
            donkey.setCustomName(TextUtil.color("&a" + petName));
            donkey.setCustomNameVisible(true);
            
            // Tornar o burro manso
            donkey.setTamed(true);
            donkey.setAdult();
            
            // Definir atributos
            if (donkey.getAttribute(Attribute.MAX_HEALTH) != null) {
                donkey.getAttribute(Attribute.MAX_HEALTH).setBaseValue(maxHealth);
                donkey.setHealth(health);
            }
            
            if (donkey.getAttribute(Attribute.MOVEMENT_SPEED) != null) {
                donkey.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(speed);
            }
            
            // Definir proprietário
            Player owner = plugin.getServer().getPlayer(ownerId);
            if (owner != null) {
                donkey.setOwner(owner);
            }
            
            // Habilitar inventário apenas em nível 5
            donkey.setCarryingChest(hasLevel5Ability());
        }
    }

    @Override
    protected void performLevel5AbilityWhileMounted(Player player) {
        // Habilidade de nível 5: Pode armazenar itens quando montado
        if (entity instanceof Donkey && hasLevel5Ability()) {
            // Permitir acesso ao inventário do burro
            // Esta lógica é apenas para verificar se o jogador pode acessar o inventário
            // O inventário real do burro já é gerenciado pelo jogo
            if (!inventoryAccessible) {
                Donkey donkey = (Donkey) entity;
                donkey.setCarryingChest(true);
                inventoryAccessible = true;
            }
        }
    }
    
    @Override
    public boolean mount(Player player) {
        boolean mounted = super.mount(player);
        
        if (mounted && hasLevel5Ability()) {
            // Enviar mensagem informando sobre a habilidade de armazenamento
            new BukkitRunnable() {
                @Override
                public void run() {
                    player.sendMessage(plugin.formatMessage("&aClique com shift + botão direito para acessar o inventário do seu burro!"));
                }
            }.runTaskLater(plugin, 20L); // Enviar a mensagem após 1 segundo
        }
        
        return mounted;
    }
    
    @Override
    public void dismount(Player player) {
        super.dismount(player);
        inventoryAccessible = false;
    }
    
    @Override
    public void despawn() {
        inventoryAccessible = false;
        super.despawn();
    }
}