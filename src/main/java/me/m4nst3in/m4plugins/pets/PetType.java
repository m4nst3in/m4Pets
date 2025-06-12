package me.m4nst3in.m4plugins.pets;

public enum PetType {
    // Montarias
    PIG("mount"),
    HORSE("mount"),
    DONKEY("mount"),
    SHEEP("mount"),
    COW("mount"),
    
    
    // Guerreiros (em desenvolvimento)
    WOLF("warriors"),
    
    // Novos pets montáveis
    MARE("mount"),
    IRON_GOLEM("warriors"),
    SKELETON("warriors"),
    ZOMBIE("warriors"),
    VINDICATOR("warriors"),
    
    // Trabalhadores (em desenvolvimento)
    VILLAGER("workers"),
    BEE("workers"),
    ALLAY("workers"),
    
    // Decorativos (em desenvolvimento)
    CAT("decorative"),
    PARROT("decorative"),
    RABBIT("decorative"),
    AXOLOTL("decorative");
    
    private final String configCategory;
    
    PetType(String configCategory) {
        this.configCategory = configCategory;
    }
    
    public String getConfigCategory() {
        return configCategory;
    }
    
    public static boolean isMountType(PetType type) {
        return type.configCategory.equals("mount");
    }
    
    public static boolean isWarriorType(PetType type) {
        return type.configCategory.equals("warriors");
    }
    
    public static boolean isWorkerType(PetType type) {
        return type.configCategory.equals("workers");
    }
    
    public static boolean isDecorativeType(PetType type) {
        return type.configCategory.equals("decorative");
    }
}