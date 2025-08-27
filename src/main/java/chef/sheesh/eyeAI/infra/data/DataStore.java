package chef.sheesh.eyeAI.infra.data;

public interface DataStore {
    void connect();
    void disconnect();
    void saveData(String key, Object data);
    Object loadData(String key);
}
