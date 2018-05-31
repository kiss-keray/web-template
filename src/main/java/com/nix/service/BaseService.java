package com.nix.service;
import com.nix.model.base.BaseModel;
import com.nix.util.SQLUtil;
import com.nix.util.log.LogKit;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * @author 11723
 * @date 2017/5/4
 *
 * service实现接口的基本类
 * 实现了对继承{@link BaseModel}的model基础的增删改查接口
 */
@Service
public class BaseService<M extends BaseModel<M>>{


    /**
     * 反射执行执行dao方法（有时间改为动态代理）
     * @param methodName 需要执行的dao方法名
     * @param clazzs 方法参数对象组类类型（为了反射时找到具体方法）
     * @param objects 调用方法参数组（执行方法需要的参数）
     * @return 返回方法执行结果
     * @throws Exception 抛出反射执行过程中出现的异常
     * */
    private Object invokeMapperMethod(String methodName,Class[] clazzs,Object ... objects) throws Exception {
        LogKit.info(this.getClass(),"执行" + methodName + "方法");
        try {
            String mapperName = this.getClass().getSimpleName().replaceFirst("Service","Mapper");
            Object o = SpringContextHolder.getBean(mapperName.substring(0,1).toLowerCase() + mapperName.substring(1));
            Class<?> clazz = o.getClass();
            Method method = clazz.getMethod(methodName, clazzs);
            return method.invoke(o,objects);
        }catch (Exception e){
            throw e;
        }
    }
    /**
     * 去dao中找具体的方法
     * @param methodName 需要找的方法名
     * @param model 与dao绑定的model类
     * */
    private Object callInvoke(String methodName,M model) throws Exception {
        return invokeMapperMethod(methodName,new Class[]{BaseModel.class},model);
    }
    public M add(M model) throws Exception {
        //自动生成的id设置
        setId(model);
        model.setCreateDate(new Date());
        model.setUpdateDate(new Date());
        callInvoke("insert",model);
        return model;
    }

    private void setId(M model) throws Exception {
        Integer id = (Integer) callInvoke("maxId",model);
        model.setId(id  == null ? 1 : id + 1);
    }

    /**
     * 批量删除用户
     * @param ids id数组
     * @return
     * @throws Exception 删除失败抛出异常
     * */
    public void delete(Integer[] ids) throws Exception {
        for (Integer id:ids) {
            delete(id);
        }
    }
    /**
     * 在数据库中删除一个对象
     * @param id 需要删除对象的id值
     * @return
     * @throws Exception 删除失败抛出异常
     * */
    public void delete(Integer id) throws Exception {
        invokeMapperMethod("delete",new Class[]{Integer.class},id);
    }
    /**
     *
     * 更新数据库中某个对象
     * @param model 需要更新的对象
     * @return
     * @throws Exception 修改失败抛出异常
     *
     * */
    public M update(M model) throws Exception {
        model.setUpdateDate(new Date());
        callInvoke("update",model);
        return model;
    }


    /**
     *
     * 查找某个相应条件的对象列表
     * @param page 列表分页页数
     * @param size 当前页需要查询对象的最大数量
     * @param order 查找对象时按照哪个字段排序
     * @param sort 排序时的排序方式（升序 降序）
     * @param conditionsSql 查找列表时的sql条件  sql语=语句里where后面的部分都写在改字符串里
     * @return 返回符合条件的对象列表 但查找失败时返回null
     * */
    public List<M> list(String tables,Integer page,Integer size,String order,String sort,String conditionsSql){
        if (tables == null || tables.isEmpty()) {
            tables = this.getClass().getSimpleName().replaceFirst("Service","");
        }
        try {
            Object find = invokeMapperMethod("list", new Class[]{String.class,Integer.class,Integer.class,String.class,String.class,String.class},
                    tables,SQLUtil.getOffset(page,size), size,order,sort,conditionsSql);
            return (List<M>) find;
        }catch (Exception e){
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public List<M> list(Integer page,Integer size,String order,String sort,String conditionsSql){
        String tables = this.getClass().getSimpleName().replaceFirst("Service","");
        return list(tables,page,size,order,sort,conditionsSql);
    }

    public List<M> select(String conditionsSql,Object ... param) {
        return list(null,null,null,null,SQLUtil.sqlFormat(conditionsSql,param));
    }

    public List<M> select(boolean b,String tables,String conditionsSql,Object ... param) {
        return list(tables,null,null,null,null,SQLUtil.sqlFormat(conditionsSql,param));
    }

    /**
     *
     * 根据唯一id值查找某个对象
     * @param id 查找的id值
     * @return 返回查找到的对象 查找失败返回空值
     *
     * */
    public M findById(Integer id){
        try {
            Object find = invokeMapperMethod("select",new Class[]{Integer.class},id);
            return (M) find;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public List<M> findByOneField(String field,String content) {
        try {
            Object find = invokeMapperMethod("findByOneField",new Class[]{String.class,String.class},field,content);
            return (List<M>) find;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public Long count() throws Exception {
        return (Long) invokeMapperMethod("count",null,null);
    }

}
