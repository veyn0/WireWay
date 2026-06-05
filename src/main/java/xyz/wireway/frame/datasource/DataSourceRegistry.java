package xyz.wireway.frame.datasource;

import xyz.wireway.service.DataController;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class DataSourceRegistry {

    private final Map<Class<? extends DataSource>, Integer> sourceIdsByClass = new ConcurrentHashMap<>();

    private final Map<Integer, Supplier<DataSource>> incomingSourcesRegistry = new ConcurrentHashMap<>();

    private DataController dataController;

    public DataSourceRegistry(DataController dataController) {
        this.dataController = dataController;
    }

    public void registerDataSource(Class<? extends  DataSource> dataSource){
        DataSourceId dataSourceId = dataSource.getAnnotation(DataSourceId.class);
        if (dataSourceId==null) throw new IllegalArgumentException("DataSource must annotate @DataSourceId");
        int id = dataSourceId.datasourceId();
        if(incomingSourcesRegistry.containsKey(id) || sourceIdsByClass.containsKey(dataSource)) throw new IllegalArgumentException("datasourceId already exists");
        sourceIdsByClass.put(dataSource, id);
        incomingSourcesRegistry.put(id, () -> {
            try {
                return dataSource.getDeclaredConstructor().newInstance();
            } catch (Exception e){
                throw new RuntimeException(e);
            }
        });
    }

    public int getDataSourceId(DataSource dataSource){
        return sourceIdsByClass.get(dataSource.getClass());
    }

    public DataSource createDataSource(int id){
        Supplier<DataSource> supplier = incomingSourcesRegistry.get(id);
        if (supplier == null) throw new IllegalArgumentException("DataSource id " + id + " not registered");

        DataSource dataSource = supplier.get();
        dataSource.inject(dataController);

        return dataSource;
    }

}
