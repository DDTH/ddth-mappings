package com.github.ddth.mappings.cql;

import java.nio.ByteBuffer;
import java.text.MessageFormat;
import java.util.ArrayList;
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
import com.github.ddth.mappings.AbstractMappingOneOneDao;
import com.github.ddth.mappings.MappingBo;
import com.github.ddth.mappings.utils.MappingsUtils;

/**
 * C*QL-implementation of 1-1 mapping.
 *
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since 0.1.0
 */
public class CqlMappingOneOneDao<O, T> extends AbstractMappingOneOneDao<O, T> {

    public CqlMappingOneOneDao(Class<O> objectClass, Class<T> targetClass) {
        super(objectClass, targetClass);
    }

    private CqlDelegator cqlDelegator;

    private String tableData = "mapoo_data";

    public CqlDelegator getCqlDelegator() {
        return cqlDelegator;
    }

    public CqlMappingOneOneDao<O, T> setCqlDelegator(CqlDelegator cqlDelegator) {
        this.cqlDelegator = cqlDelegator;
        return this;
    }

    public String getTableData() {
        return tableData;
    }

    public CqlMappingOneOneDao<O, T> setTableData(String tableData) {
        this.tableData = tableData;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CqlMappingOneOneDao<O, T> init() {
        super.init();

        pstmDeleteData = cqlDelegator
                .prepareStatement(MessageFormat.format(CQL_DELETE_DATA, tableData));
        pstmDeleteDataIfExists = cqlDelegator
                .prepareStatement(MessageFormat.format(CQL_DELETE_DATA_IF_EXISTS, tableData));

        pstmInsertData = cqlDelegator
                .prepareStatement(MessageFormat.format(CQL_INSERT_DATA, tableData));

        pstmSelectData = cqlDelegator
                .prepareStatement(MessageFormat.format(CQL_SELECT_DATA, tableData));

        return this;
    }

    private PreparedStatement pstmDeleteData, pstmDeleteDataIfExists;
    private PreparedStatement pstmInsertData;
    private PreparedStatement pstmSelectData;

    private final static String COL_DATA_NAMESPACE = "m_namespace";
    private final static String COL_DATA_TYPE = "m_type";
    private final static String COL_DATA_KEY = "m_key";
    private final static String COL_DATA_DATA = "m_data";
    private final static String[] _COL_DATA_ALL = { COL_DATA_NAMESPACE, COL_DATA_TYPE, COL_DATA_KEY,
            COL_DATA_DATA };
    private final static String[] _WHERE_DATA = { COL_DATA_NAMESPACE + "=?", COL_DATA_TYPE + "=?",
            COL_DATA_KEY + "=?" };

    private final static String CQL_DELETE_DATA = "DELETE FROM {0} WHERE "
            + StringUtils.join(_WHERE_DATA, " AND ");
    private final static String CQL_DELETE_DATA_IF_EXISTS = "DELETE FROM {0} WHERE "
            + StringUtils.join(_WHERE_DATA, " AND ") + " IF EXISTS";
    private final static String CQL_INSERT_DATA = "INSERT INTO {0} ("
            + StringUtils.join(_COL_DATA_ALL, ",") + ") VALUES ("
            + StringUtils.repeat("?", ",", _COL_DATA_ALL.length) + ")";
    private final static String CQL_SELECT_DATA = "SELECT * FROM {0} WHERE "
            + StringUtils.join(_WHERE_DATA, " AND ");

    public final static String DATA_TYPE_OBJ_TARGET = "obj:target";
    public final static String DATA_TYPE_TARGET_OBJ = "target:obj";
    public final static String STATS_MAPPING = "mappings-oo";
    public final static String STATS_KEY_TOTAL_ITEMS = "total-items";

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

    /**
     * Create a new mapping. Sub-class may override this method to customize its
     * business logic.
     * 
     * @param row
     * @return
     */
    @SuppressWarnings("unchecked")
    protected MappingBo<O, T> createMappingBo(Row row) {
        if (row == null) {
            return null;
        }
        String namespace = row.getString(COL_DATA_NAMESPACE);
        String type = row.getString(COL_DATA_TYPE);
        if (StringUtils.equalsIgnoreCase(type, DATA_TYPE_OBJ_TARGET)) {
            // O obj = cqlDelegator.fetchValue(row, COL_DATA_KEY,
            // getObjectClass());
            O obj = deserializeObject(row.getString(COL_DATA_KEY));
            Object[] values = cqlDelegator.fetchAndDecodeValues(row, COL_DATA_DATA,
                    getTargetClass(), Long.class);
            T target = (T) values[0];
            Long timestamp = (Long) values[1];
            return createMappingBo(namespace, obj, target).setTimestamp(timestamp.longValue());
        } else if (StringUtils.equalsIgnoreCase(type, DATA_TYPE_TARGET_OBJ)) {
            // T target = cqlDelegator.fetchValue(row, COL_DATA_KEY,
            // getTargetClass());
            T target = deserializeTarget(row.getString(COL_DATA_KEY));
            Object[] values = cqlDelegator.fetchAndDecodeValues(row, COL_DATA_DATA,
                    getObjectClass(), Long.class);
            O obj = (O) values[0];
            Long timestamp = (Long) values[1];
            return createMappingBo(namespace, obj, target).setTimestamp(timestamp.longValue());
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
    protected MappingBo<O, T> storageGetMappingObjTarget(String namespace, O obj) {
        Row row = cqlDelegator.selectOneRow(pstmSelectData, namespace, DATA_TYPE_OBJ_TARGET,
                serializeObject(obj));
        return createMappingBo(row);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected MappingBo<O, T> storageGetMappingTargetObj(String namespace, T target) {
        Row row = cqlDelegator.selectOneRow(pstmSelectData, namespace, DATA_TYPE_TARGET_OBJ,
                serializeTarget(target));
        return createMappingBo(row);
    }

    /**
     * <ul>
     * <li>Remove existing {@code old-target <- object}, if any.</li>
     * <li>Store mapping {@code object -> target}, will override any existing
     * {@code object ->
     * old-target}.</li>
     * <li>Store mapping {@code target <- object}.</li>
     * <li>All operations above are within a logged batch.</li>
     * <li>Finally, update stats if needed.</li>
     * </ul>
     */
    @Override
    protected MappingsUtils.DaoResult storageMap(MappingBo<O, T> mappingToAdd,
            MappingBo<O, T> existingOT) {
        final long now = System.currentTimeMillis();
        final String NAMESPACE = mappingToAdd.getNamespace();
        final O OBJ = mappingToAdd.getObject();
        final T TARGET = mappingToAdd.getTarget();

        ByteBuffer targetTime = MappingsUtils.seConcatToByteBuffer(
                SerializationUtils.toJsonString(TARGET), SerializationUtils.toJsonString(now));
        ByteBuffer objTime = MappingsUtils.seConcatToByteBuffer(
                SerializationUtils.toJsonString(OBJ), SerializationUtils.toJsonString(now));

        List<Statement> stmList = new ArrayList<>();
        if (existingOT != null) {
            stmList.add(CqlUtils.bindValues(pstmDeleteData, NAMESPACE, DATA_TYPE_TARGET_OBJ,
                    serializeTarget(existingOT.getTarget())));
        }
        stmList.add(CqlUtils.bindValues(pstmInsertData, NAMESPACE, DATA_TYPE_OBJ_TARGET,
                serializeObject(OBJ), targetTime));
        stmList.add(CqlUtils.bindValues(pstmInsertData, NAMESPACE, DATA_TYPE_TARGET_OBJ,
                serializeTarget(TARGET), objTime));
        ResultSet rs = cqlDelegator.executeBatch(stmList.toArray(new Statement[0]));
        if (rs.wasApplied()) {
            if (existingOT == null) {
                storageUpdateStats(NAMESPACE, STATS_KEY_TOTAL_ITEMS, 1);
            }
            return new MappingsUtils.DaoResult(DaoOperationStatus.SUCCESSFUL);
        } else {
            return new MappingsUtils.DaoResult(DaoOperationStatus.ERROR);
        }
    }

    /**
     * <ul>
     * <li>Remove mapping {@code object -> target}.</li>
     * <li>Remove mapping {@code target <- object}.</li>
     * <li>All operations above are within a logged batch.</li>
     * <li>Finally, update stats if needed.</li>
     * </ul>
     */
    @Override
    protected MappingsUtils.DaoResult storageUnmap(MappingBo<O, T> mappingToRemove) {
        final String NAMESPACE = mappingToRemove.getNamespace();
        final Object OBJ = serializeObject(mappingToRemove.getObject());
        final Object TARGET = serializeTarget(mappingToRemove.getTarget());

        Statement stmDeleteObjTarget = CqlUtils.bindValues(pstmDeleteDataIfExists, NAMESPACE,
                DATA_TYPE_OBJ_TARGET, OBJ);
        Statement stmDeleteTargetObj = CqlUtils.bindValues(pstmDeleteData, NAMESPACE,
                DATA_TYPE_TARGET_OBJ, TARGET);
        ResultSet rs = cqlDelegator.executeBatch(stmDeleteObjTarget, stmDeleteTargetObj);
        if (rs.wasApplied()) {
            storageUpdateStats(NAMESPACE, STATS_KEY_TOTAL_ITEMS, -1);
            return new MappingsUtils.DaoResult(DaoOperationStatus.SUCCESSFUL);
        } else {
            return new MappingsUtils.DaoResult(DaoOperationStatus.NOT_FOUND);
        }
    }

    private void storageUpdateStats(String namespace, String key, long value) {
        cqlDelegator.updateStats(STATS_MAPPING, namespace, key, value);
    }
}
