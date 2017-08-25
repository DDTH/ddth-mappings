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
import com.github.ddth.mappings.AbstractMappingManyOneDao;
import com.github.ddth.mappings.MappingBo;
import com.github.ddth.mappings.utils.MappingsUtils;

/**
 * C*QL-implementation of n-1 mapping.
 *
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since 0.1.0
 */
public class CqlMappingManyOneDao<O, T> extends AbstractMappingManyOneDao<O, T> {

    public CqlMappingManyOneDao(Class<O> objectClass, Class<T> targetClass) {
        super(objectClass, targetClass);
    }

    private CqlDelegator cqlDelegator;

    private String tableObjTarget = "mapmo_objtarget";
    private String tableTargetObj = "mapmo_targetobj";

    public CqlDelegator getCqlDelegator() {
        return cqlDelegator;
    }

    public CqlMappingManyOneDao<O, T> setCqlDelegator(CqlDelegator cqlDelegator) {
        this.cqlDelegator = cqlDelegator;
        return this;
    }

    public String getTableObjTarget() {
        return tableObjTarget;
    }

    public CqlMappingManyOneDao<O, T> setTableObjTarget(String tableObjTarget) {
        this.tableObjTarget = tableObjTarget;
        return this;
    }

    public String getTableTargetObj() {
        return tableTargetObj;
    }

    public CqlMappingManyOneDao<O, T> setTableTargetObj(String tableTargetObj) {
        this.tableTargetObj = tableTargetObj;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CqlMappingManyOneDao<O, T> init() {
        super.init();

        pstmDeleteOT = cqlDelegator
                .prepareStatement(MessageFormat.format(CQL_DELETE_OT, tableObjTarget));
        pstmDeleteTO = cqlDelegator
                .prepareStatement(MessageFormat.format(CQL_DELETE_TO, tableTargetObj));

        pstmInsertOT = cqlDelegator
                .prepareStatement(MessageFormat.format(CQL_INSERT_OT, tableObjTarget));
        pstmInsertTO = cqlDelegator
                .prepareStatement(MessageFormat.format(CQL_INSERT_TO, tableTargetObj));

        pstmSeleteOT = cqlDelegator
                .prepareStatement(MessageFormat.format(CQL_SELECT_OT, tableObjTarget));
        pstmSeleteTOMulti = cqlDelegator
                .prepareStatement(MessageFormat.format(CQL_SELECT_TO_MULTI, tableTargetObj));

        return this;
    }

    private PreparedStatement pstmDeleteOT, pstmDeleteTO;
    private PreparedStatement pstmInsertOT, pstmInsertTO;
    private PreparedStatement pstmSeleteOT, pstmSeleteTOMulti;

    private final static String COL_NAMESPACE = "m_namespace";
    private final static String COL_OBJECT = "m_object";
    private final static String COL_TARGET = "m_target";
    private final static String COL_DATA = "m_data";

    private final static String[] _COLS_OT_ALL = { COL_NAMESPACE, COL_OBJECT, COL_DATA };
    private final static String[] _COLS_TO_ALL = { COL_NAMESPACE, COL_TARGET, COL_OBJECT,
            COL_DATA };
    private final static String[] _WHERE_OT = { COL_NAMESPACE + "=?", COL_OBJECT + "=?" };
    private final static String[] _WHERE_TO_SINGLE = { COL_NAMESPACE + "=?", COL_TARGET + "=?",
            COL_OBJECT + "=?" };
    private final static String[] _WHERE_TO_MULTI = { COL_NAMESPACE + "=?", COL_TARGET + "=?" };

    private final static String CQL_DELETE_OT = "DELETE FROM {0} WHERE "
            + StringUtils.join(_WHERE_OT, " AND ");
    private final static String CQL_DELETE_TO = "DELETE FROM {0} WHERE "
            + StringUtils.join(_WHERE_TO_SINGLE, " AND ");

    private final static String CQL_SELECT_OT = "SELECT " + StringUtils.join(_COLS_OT_ALL, ",")
            + " FROM {0} WHERE " + StringUtils.join(_WHERE_OT, " AND ");
    private final static String CQL_SELECT_TO_MULTI = "SELECT "
            + StringUtils.join(_COLS_TO_ALL, ",") + " FROM {0} WHERE "
            + StringUtils.join(_WHERE_TO_MULTI, " AND ");

    private final static String CQL_INSERT_OT = "INSERT INTO {0} ("
            + StringUtils.join(_COLS_OT_ALL, ",") + ") VALUES ("
            + StringUtils.repeat("?", ",", _COLS_OT_ALL.length) + ")";
    private final static String CQL_INSERT_TO = "INSERT INTO {0} ("
            + StringUtils.join(_COLS_TO_ALL, ",") + ") VALUES ("
            + StringUtils.repeat("?", ",", _COLS_TO_ALL.length) + ")";

    public final static String DATA_TYPE_OBJ_TARGET = "obj:target";
    public final static String DATA_TYPE_TARGET_OBJ = "target:obj";
    public final static String STATS_MAPPING = "mappings-mo";
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

    /**
     * Create a new mapping. Sub-class may override this method to customize its
     * business logic.
     * 
     * @param row
     * @param type
     * @return
     */
    @SuppressWarnings("unchecked")
    protected MappingBo<O, T> createMappingBo(Row row, String type) {
        if (row == null) {
            return null;
        }
        String namespace = row.getString(COL_NAMESPACE);
        if (StringUtils.equalsIgnoreCase(type, DATA_TYPE_OBJ_TARGET)) {
            // O obj = cqlDelegator.fetchValue(row, COL_OBJECT,
            // getObjectClass());
            O obj = deserializeObject(row.getString(COL_OBJECT));

            Object[] values = cqlDelegator.fetchAndDecodeValues(row, COL_DATA, getTargetClass(),
                    Long.class);
            T target = (T) values[0];
            Long timestamp = (Long) values[1];
            return createMappingBo(namespace, obj, target).setTimestamp(timestamp.longValue());
        } else if (StringUtils.equalsIgnoreCase(type, DATA_TYPE_TARGET_OBJ)) {
            // T target = cqlDelegator.fetchValue(row, COL_TARGET,
            // getTargetClass());
            // O obj = cqlDelegator.fetchValue(row, COL_OBJECT,
            // getObjectClass());
            T target = deserializeTarget(row.getString(COL_TARGET));
            O obj = deserializeObject(row.getString(COL_OBJECT));

            Object[] values = cqlDelegator.fetchAndDecodeValues(row, COL_DATA, Long.class);
            Long timestamp = (Long) values[0];
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
        Row row = cqlDelegator.selectOneRow(pstmSeleteOT, namespace, serializeObject(obj));
        return createMappingBo(row, DATA_TYPE_OBJ_TARGET);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Collection<MappingBo<O, T>> storageGetMappingsTargetObjs(String namespace, T target) {
        Collection<MappingBo<O, T>> result = new HashSet<>();
        ResultSet rs = cqlDelegator.select(pstmSeleteTOMulti, namespace, serializeTarget(target));
        rs.forEach(row -> result.add(createMappingBo(row, DATA_TYPE_TARGET_OBJ)));
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
            MappingBo<O, T> existingOT, Collection<MappingBo<O, T>> existingTO) {
        final long now = System.currentTimeMillis();
        final String NAMESPACE = mappingToAdd.getNamespace();
        final O obj = mappingToAdd.getObject();
        final Object seObj = serializeObject(obj);
        final T target = mappingToAdd.getTarget();
        final Object seTarget = serializeTarget(target);

        /*
         * for mapping {target -> object}
         */
        ByteBuffer data = MappingsUtils.seConcatToByteBuffer(SerializationUtils.toJsonString(now));

        /*
         * for mapping {object -> target}
         */
        ByteBuffer targetData = MappingsUtils.seConcatToByteBuffer(
                SerializationUtils.toJsonString(target), SerializationUtils.toJsonString(now));

        List<Statement> stmList = new ArrayList<>();
        if (existingOT != null) {
            stmList.add(CqlUtils.bindValues(pstmDeleteTO, NAMESPACE,
                    serializeTarget(existingOT.getTarget()),
                    serializeObject(existingOT.getObject())));
        }
        stmList.add(CqlUtils.bindValues(pstmInsertOT, NAMESPACE, seObj, targetData));
        stmList.add(CqlUtils.bindValues(pstmInsertTO, NAMESPACE, seTarget, seObj, data));
        ResultSet rs = cqlDelegator.executeBatch(stmList.toArray(new Statement[0]));
        if (rs.wasApplied()) {
            if (existingOT == null) {
                storageUpdateStats(NAMESPACE, STATS_KEY_TOTAL_OBJS, 1);
                if (existingTO == null || existingTO.size() == 0) {
                    storageUpdateStats(NAMESPACE, STATS_KEY_TOTAL_TARGETS, 1);
                }
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
            MappingBo<O, T> existingOT, Collection<MappingBo<O, T>> existingTO) {
        final String NAMESPACE = mappingToRemove.getNamespace();
        final Object OBJ = serializeObject(mappingToRemove.getObject());
        // final Object TARGET = serializeTarget(mappingToRemove.getTarget());
        final T TARGET = mappingToRemove.getTarget();

        Statement stmDeleteObjTarget = CqlUtils.bindValues(pstmDeleteOT, NAMESPACE, OBJ);
        Statement stmDeleteTargetObj = CqlUtils.bindValues(pstmDeleteTO, NAMESPACE,
                serializeTarget(TARGET), OBJ);
        ResultSet rs = cqlDelegator.executeBatch(stmDeleteObjTarget, stmDeleteTargetObj);
        if (rs.wasApplied()) {
            if (existingOT != null && TARGET.equals(existingOT.getTarget())) {
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
