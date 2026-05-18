package Stores;

public interface ProcessedEventStore {

    /*
     * public boolean tryStartProcessing(String eventId, String handlerName) {
     * return false;
     * }
     */

    void markAsProcessed(String eventId, String handlerName);

    boolean exists(String eventId, String handlerName);

}

/*
 * Flujo
 * if (!tryStartProcessing) → skip
 * 
 * try:
 * apply()
 * markAsProcessed()
 * catch:
 * no marcar → retry
 * 
 * 
 * 
 * 
 * 🔧 Alternativa mejor
 * boolean markAsProcessedIfNotExists(String eventId, String handlerName);
 * 🧠 Comportamiento
 * - si NO existe → lo guarda → return true
 * - si YA existe → return false
 */