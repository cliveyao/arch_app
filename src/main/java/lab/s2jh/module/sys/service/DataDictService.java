package lab.s2jh.module.sys.service;

import java.util.List;
import java.util.Map;

import lab.s2jh.core.dao.jpa.BaseDao;
import lab.s2jh.core.service.BaseService;
import lab.s2jh.module.sys.dao.DataDictDao;
import lab.s2jh.module.sys.entity.DataDict;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

@Service
@Transactional
public class DataDictService extends BaseService<DataDict, Long> {

    private static final Logger logger = LoggerFactory.getLogger(DataDictService.class);

    @Autowired
    private DataDictDao dataDictDao;

    @Override
    protected BaseDao<DataDict, Long> getEntityDao() {
        return dataDictDao;
    }

    @Transactional(readOnly = true)
    public List<DataDict> findAllCached() {
        return dataDictDao.findAllCached();
    }

    /**
     * Returns the corresponding data dictionary -based subset of the primary key
     * @param Id primary key
     * @return
     */
    @Transactional(readOnly = true)
    public List<DataDict> findChildrenById(Long id) {
        return findChildrenById(id, false);
    }

    /**
     * Returns the corresponding data dictionary -based subset of the primary key
     * @param Id primary key
     Whether the association * @param withFlatChildren flat structure returns the child node data
     * @return
     */
    @Transactional(readOnly = true)
    public List<DataDict> findChildrenById(Long id, boolean withFlatChildren) {
        return findChildrens(dataDictDao.findOne(id), withFlatChildren);
    }

    /**
     * Based directly on the root primaryKey returns the corresponding set of data dictionary
     * @param PrimaryKey the root primaryKey
     * @return
     */
    @Transactional(readOnly = true)
    public List<DataDict> findChildrenByRootPrimaryKey(String primaryKey) {
        return findChildrenByRootPrimaryKey(primaryKey, false);
    }

    /**
     * Based directly on the root primaryKey returns the corresponding set of data dictionary
     * @param PrimaryKey the root primaryKey
     Whether the association * @param withFlatChildren flat structure returns the child node data
     * @return
     */
    @Transactional(readOnly = true)
    public List<DataDict> findChildrenByRootPrimaryKey(String primaryKey, boolean withFlatChildren) {
        return findChildrens(dataDictDao.findByRootPrimaryKey(primaryKey), withFlatChildren);
    }

    private List<DataDict> findChildrens(DataDict parent, boolean withFlatChildren) {
        if (parent == null) {
            return null;
        }
        List<DataDict> roots = dataDictDao.findEnabledChildrenByParentId(parent.getId());
        if (withFlatChildren) {
            List<DataDict> dataDicts = Lists.newArrayList(roots);
            for (DataDict dataDict : roots) {
                List<DataDict> chidren = dataDictDao.findEnabledChildrenByParentId(dataDict.getId());
                dataDicts.addAll(chidren);
            }
            return dataDicts;
        } else {
            return roots;
        }
    }

    /**
     * Map directly returns the corresponding key-value data structure based on the root primaryKey
     * Note : If the association to return the child nodes, make sure primaryKey uniqueness of all nodes , otherwise the data can not be expected to cover a problem
     * @param PrimaryKey the root primaryKey
     * @return
     */
    @Transactional(readOnly = true)
    public Map<String, String> findMapDataByRootPrimaryKey(String primaryKey) {
        return findMapDataByRootPrimaryKey(primaryKey, false);
    }

    /**
     * Based on the primary key returns the corresponding key-value data structure Map
     * Note : If the association to return the child nodes, make sure primaryKey uniqueness of all nodes , otherwise the data can not be expected to cover a problem
     * @param Id primary key
     * @return
     */
    @Transactional(readOnly = true)
    public Map<String, String> findMapDataById(Long id) {
        return findMapDatas(dataDictDao.findOne(id), false);
    }

    /**
     * Map directly returns the corresponding key-value data structure based on the root primaryKey
     * Note : If the association to return the child nodes, make sure primaryKey uniqueness of all nodes , otherwise the data can not be expected to cover a problem
     * @param PrimaryKey the root primaryKey
     Whether the association * @param withFlatChildren flat structure returns the child node data
     * @return
     */
    @Transactional(readOnly = true)
    public Map<String, String> findMapDataByRootPrimaryKey(String primaryKey, boolean withFlatChildren) {
        return findMapDatas(dataDictDao.findByRootPrimaryKey(primaryKey), withFlatChildren);
    }

    private Map<String, String> findMapDatas(DataDict parent, boolean withFlatChildren) {
        Map<String, String> dataMap = Maps.newLinkedHashMap();
        List<DataDict> dataDicts = findChildrens(parent, withFlatChildren);
        if (dataDicts != null) {
            for (DataDict dataDict : dataDicts) {
                dataMap.put(dataDict.getPrimaryKey(), dataDict.getPrimaryValue());
            }
        }
        return dataMap;
    }

    @Transactional(readOnly = true)
    public Map<String, DataDict> findMapObjectByRootPrimaryKey(String primaryKey) {
        Map<String, DataDict> dataMap = Maps.newLinkedHashMap();
        List<DataDict> dataDicts = findChildrenByRootPrimaryKey(primaryKey);
        if (dataDicts != null) {
            for (DataDict dataDict : dataDicts) {
                dataMap.put(dataDict.getPrimaryKey(), dataDict);
            }
        } else {
            logger.warn("Undefined DataDict for primaryKey: {}", primaryKey);
        }
        return dataMap;
    }
}
