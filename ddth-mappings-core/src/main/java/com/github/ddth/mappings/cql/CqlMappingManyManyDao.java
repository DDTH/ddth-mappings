package com.github.ddth.mappings.cql;

import java.nio.ByteBuffer;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.github.ddth.commons.utils.SerializationUtils;
import com.github.ddth.cql.CqlUtils;
import com.github.ddth.dao.utils.DaoResult.DaoOperationStatus;
import com.github.ddth.mappings.AbstractMappingManyManyDao;
import com.github.ddth.mappings.MappingBo;
import com.github.ddth.mappings.utils.MappingsUtils;

/**
 * C*QL-implementation of 1-1 mapping.
 *
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since 0.1.0
 */
public class CqlMappingManyManyDao<O, T> extends AbstractMappingManyManyDao<O, T> {

    public CqlMappingManyManyDao(Class<O> objectClass, Class<T> targetClass) {
        super(objectClass, targetClass);
    }

    private CqlDelegator cqlDelegator;

    private String tableData = "mapmm_data";

    public CqlDelegator getCqlDelegator() {
        return cqlDelegator;
    }

    public CqlMappingManyManyDao<O, T> setCqlDelegator(CqlDelegator cqlDelegator) {
        this.cqlDelegator = cqlDelegator;
        return this;
    }

    public String getTableData() {
        return tableData;
    }

    public CqlMappingManyManyDao<O, T> setTableData(String tableData) {
        this.tableData = tableData;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CqlMappingManyManyDao<O, T> init() {
        super.init();

        pstmDeleteDataSingle = cqlDelegator
                .prepareStatement(MessageFormat.format(CQL_DELETE_DATA_SINGLE, tableData));

        pstmInsertData = cqlDelegator
                .prepareStatement(MessageFormat.format(CQL_INSERT_DATA, tableData));

        pstmSeleteDataMultiple = cqlDelegator
                .prepareStatement(MessageFormat.format(CQL_SELECT_DATA_MULTIPLE, tableData));

        return this;
    }

    private PreparedStatement pstmDeleteDataSingle;
    private PreparedStatement pstmInsertData;
    private PreparedStatement pstmSeleteDataMultiple;

    private final static String COL_NAMESPACE = "m_namespace";
    private final static String COL_TYPE = "m_type";
    private final static String COL_KEY = "m_key";
    private final static String COL_VALUE = "m_value";
    private final static String COL_DATA = "m_data";
    private final static String[] _COL_ALL = { COL_NAMESPACE, COL_TYPE, COL_KEY, COL_VALUE,
            COL_DATA };
    private final static String[] _WHERE_DATA_SINGLE = { COL_NAMESPACE + "=?", COL_TYPE + "=?",
            COL_KEY + "=?", COL_VALUE + "=?" };
    private final static String[] _WHERE_DATA_MULTIPLE = { COL_NAMESPACE + "=?", COL_TYPE + "=?",
            COL_KEY + "=?" };

    private final static String CQL_DELETE_DATA_SINGLE = "DELETE FROM {0} WHERE "
            + StringUtils.join(_WHERE_DATA_SINGLE, " AND ");

    private final static String CQL_INSERT_DATA = "INSERT INTO {0} ("
            + StringUtils.join(_COL_ALL, ",") + ") VALUES ("
            + StringUtils.repeat("?", ",", _COL_ALL.length) + ")";
    private final static String CQL_SELECT_DATA_MULTIPLE = "SELECT "
            + StringUtils.join(_COL_ALL, ",") + " FROM {0} WHERE "
            + StringUtils.join(_WHERE_DATA_MULTIPLE, " AND ");

    public final static String DATA_TYPE_OBJ_TARGET = "obj:target";
    public final static String DATA_TYPE_TARGET_OBJ = "target:obj";
    public final static String STATS_MAPPING = "mappings-mm";
    public final static String STATS_KEY_TOTAL_OBJS = "total-objs";
    public final static String STATS_KEY_TOTAL_TARGETS = "total-targets";

    /**
     * {@inheritDoc}
     */
    @Override
    protected String serializeObject(O obj) {
        return SerializationUtils.toJsonString(obj);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String serializeTarget(T target) {
        return SerializationUtils.toJsonString(target);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected O deserializeObject(Object obj) {
        return SerializationUtils.fromJsonString(obj.toString(), getObjectClass(),
                getClass().getClassLoader());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected T deserializeTarget(Object target) {
        return SerializationUtils.fromJsonString(target.toString(), getTargetClass(),
                getClass().getClassLoader());
    }

    private MappingBo<O, T> createMappingBo(Row row) {
        if (row != null) {
            String namespace = row.getString(COL_NAMESPACE);
            String type = row.getString(COL_TYPE);
            if (StringUtils.equalsIgnoreCase(type, DATA_TYPE_OBJ_TARGET)) {
                // O obj = cqlDelegator.fetchValue(row, COL_KEY,
                // getObjectClass());
                // T target = cqlDelegator.fetchValue(row, COL_VALUE,
                // getTargetClass());
                O obj = deserializeObject(row.getString(COL_KEY));
                T target = deserializeTarget(row.getString(COL_VALUE));

                Object[] values = cqlDelegator.fetchAndDecodeValues(row, COL_DATA, Long.class);
                Long timestamp = (Long) values[0];
                return createMappingBo(namespace, obj, target).setTimestamp(timestamp.longValue());
            } else if (StringUtils.equalsIgnoreCase(type, DATA_TYPE_TARGET_OBJ)) {
                // O obj = cqlDelegator.fetchValue(row, COL_VALUE,
                // getObjectClass());
                // T target = cqlDelegator.fetchValue(row, COL_KEY,
                // getTargetClass());
                O obj = deserializeObject(row.getString(COL_VALUE));
                T target = deserializeTarget(row.getString(COL_KEY));

                Object[] values = cqlDelegator.fetchAndDecodeValues(row, COL_DATA, Long.class);
                Long timestamp = (Long) values[0];
                return createMappingBo(namespace, obj, target).setTimestamp(timestamp.longValue());
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Long> getStats(String namespace) {
        return cqlDelegator.getAllStats(STATS_MAPPING, namespace);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Collection<MappingBo<O, T>> storageGetMappingsObjTargets(String namespace, O obj) {
        Collection<MappingBo<O, T>> result = new HashSet<>();
        ResultSet rs = cqlDelegator.select(pstmSeleteDataMultiple, namespace, DATA_TYPE_OBJ_TARGET,
                serializeObject(obj));
        rs.forEach(row -> result.add(createMappingBo(row)));
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Collection<MappingBo<O, T>> storageGetMappingsTargetObjs(String namespace, T target) {
        Collection<MappingBo<O, T>> result = new HashSet<>();
        ResultSet rs = cqlDelegator.select(pstmSeleteDataMultiple, namespace, DATA_TYPE_TARGET_OBJ,
                serializeTarget(target));
        rs.forEach(row -> result.add(createMappingBo(row)));
        return result;
    }

    /**
     * Save mapping {@code object <-> target} to storage.
     *
     * <ul>
     * <li>Logged batch operation: store mappings {@code object -> target} &
     * {@code target ->
     * object}.</li>
     * <li>If {@code existingOT} is empty, increase stats count
     * {@link #STATS_KEY_TOTAL_OBJS}</li>
     * <li>If {@code existingTO} is empty, increase stats count
     * {@link #STATS_KEY_TOTAL_TARGETS}</li>
     * </ul>
     *
     * @param mappingToAdd
     * @param existingOT
     * @param existingTO
     * @return
     */
    @Override
    protected MappingsUtils.DaoResult storageMap(MappingBo<O, T> mappingToAdd,
            Collection<MappingBo<O, T>> existingOT, Collection<MappingBo<O, T>> existingTO) {
        final long now = System.currentTimeMillis();
        final ByteBuffer data = MappingsUtils.seConcatToByteBuffer(String.valueOf(now));
        final String NAMESPACE = mappingToAdd.getNamespace();
        final Object OBJ = serializeObject(mappingToAdd.getObject());
        final Object TARGET = serializeTarget(mappingToAdd.getTarget());

        List<Statement> stmList = new ArrayList<>();
        stmList.add(CqlUtils.bindValues(pstmInsertData, NAMESPACE, DATA_TYPE_OBJ_TARGET, OBJ,
                TARGET, data));
        stmList.add(CqlUtils.bindValues(pstmInsertData, NAMESPACE, DATA_TYPE_TARGET_OBJ, TARGET,
                OBJ, data));
        ResultSet rs = cqlDelegator.executeBatch(stmList.toArray(new Statement[0]));
        if (rs.wasApplied()) {
            if (existingOT == null || existingOT.size() == 0) {
                storageUpdateStats(NAMESPACE, STATS_KEY_TOTAL_OBJS, 1);
            }
            if (existingTO == null || existingTO.size() == 0) {
                storageUpdateStats(NAMESPACE, STATS_KEY_TOTAL_TARGETS, 1);
            }
            return new MappingsUtils.DaoResult(DaoOperationStatus.SUCCESSFUL);
        } else {
            return new MappingsUtils.DaoResult(DaoOperationStatus.ERROR);
        }
    }

    /**
     * Remove mapping {@code object <-> target} from storage.
     *
     * <ul>
     * <li>Logged batch operation: remove mappings {@code object -> target} &
     * {@code
     * target -> object}.</li>
     * <li>Decrease stats count {@link #STATS_KEY_TOTAL_OBJS} if object was the
     * last one.</li>
     * <li>Decrease stats count {@link #STATS_KEY_TOTAL_TARGETS} if target was
     * the last one.</li>
     * </ul>
     *
     * @param mappingToRemove
     * @param existingOT
     * @param existingTO
     * @return
     */
    @Override
    protected MappingsUtils.DaoResult storageUnmap(MappingBo<O, T> mappingToRemove,
            Collection<MappingBo<O, T>> existingOT, Collection<MappingBo<O, T>> existingTO) {
        final String NAMESPACE = mappingToRemove.getNamespace();
        final Object OBJ = serializeObject(mappingToRemove.getObject());
        final Object TARGET = serializeTarget(mappingToRemove.getTarget());

        Statement stmDeleteObjTarget = CqlUtils.bindValues(pstmDeleteDataSingle, NAMESPACE,
                DATA_TYPE_OBJ_TARGET, OBJ, TARGET);
        Statement stmDeleteTargetObj = CqlUtils.bindValues(pstmDeleteDataSingle, NAMESPACE,
                DATA_TYPE_TARGET_OBJ, TARGET, OBJ);
        ResultSet rs = cqlDelegator.executeBatch(stmDeleteObjTarget, stmDeleteTargetObj);
        if (rs.wasApplied()) {
            if (existingOT == null || existingOT.size() == 0
                    || (existingOT.size() == 1 && existingOT.contains(mappingToRemove))) {
                storageUpdateStats(NAMESPACE, STATS_KEY_TOTAL_OBJS, -1);
            }
            if (existingTO == null || existingTO.size() == 0
                    || (existingTO.size() == 1 && existingTO.contains(mappingToRemove))) {
                storageUpdateStats(NAMESPACE, STATS_KEY_TOTAL_TARGETS, -1);
            }
            return new MappingsUtils.DaoResult(DaoOperationStatus.SUCCESSFUL);
        } else {
            return new MappingsUtils.DaoResult(DaoOperationStatus.NOT_FOUND);
        }
    }

    private void storageUpdateStats(String namespace, String key, long value) {
        cqlDelegator.updateStats(STATS_MAPPING, namespace, key, value);
    }
}
