package com.yjxxt.crm.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.yjxxt.crm.base.BaseService;
import com.yjxxt.crm.bean.User;
import com.yjxxt.crm.bean.UserRole;
import com.yjxxt.crm.mapper.UserMapper;
import com.yjxxt.crm.mapper.UserRoleMapper;
import com.yjxxt.crm.model.UserModel;
import com.yjxxt.crm.query.UserQuery;
import com.yjxxt.crm.utils.AssertUtil;
import com.yjxxt.crm.utils.Md5Util;
import com.yjxxt.crm.utils.PhoneUtil;
import com.yjxxt.crm.utils.UserIDBase64;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

@Service
public class UserService extends BaseService<User,Integer> {

    @Resource
    private UserMapper userMapper;

    @Resource
    private UserRoleMapper userRoleMapper;

    public UserModel userLogin(String userName,String userPwd){
        //判断用户信息
        checkUserLoginParam(userName,userPwd);
        //用户是否存在
        User temp = userMapper.selectUserByName(userName);
        AssertUtil.isTrue(temp == null,"用户不存在！");
        //用户密码是否正确
        checkUserPwd(userPwd,temp.getUserPwd());
        //构建返回对象
        return builderUserInfo(temp);
    }
    //构建返回目标的对象
    private UserModel builderUserInfo(User user) {
        //实例化目标对象
        UserModel userModel = new UserModel();
        //加密
        userModel.setUserIdStr(UserIDBase64.encoderUserID(user.getId()));
        userModel.setUserName(user.getUserName());
        userModel.setTrueName(user.getTrueName());
        //返回目标对象
        return userModel;
    }

    private void checkUserPwd(String userPwd, String userPwd1) {
        //对输入的密码加密
        userPwd = Md5Util.encode(userPwd);
        //加密的密码与数据库中的密码对比
        AssertUtil.isTrue(! userPwd.equals(userPwd1),"密码不正确！");

    }
    //校验用户名密码
    private void checkUserLoginParam(String userName, String userPwd) {
        //用户不能为空
        AssertUtil.isTrue(StringUtils.isBlank(userName),"用户不能为空！");
        //密码不能为空
        AssertUtil.isTrue(StringUtils.isBlank(userPwd),"密码不能为空！");
    }

    
    public void changeUserPwd(Integer userId,String oldPassword,String newPassword,String confirmPwd){
        //用户登录了，修改密码，userId
        User user = userMapper.selectByPrimaryKey(userId);
        //密码验证
        checkPasswordParams(user,oldPassword,newPassword,confirmPwd);
        //修改密码
        user.setUserPwd(Md5Util.encode(newPassword));
        //确认密码是否修改成功
        AssertUtil.isTrue(userMapper.updateByPrimaryKeySelective(user) < 1,"修改失败！");
    }

    private void checkPasswordParams(User user, String oldPassword, String newPassword, String confirmPwd) {
        AssertUtil.isTrue(user == null,"用户未登录或用户不存在！");
        //原始密码非空
        AssertUtil.isTrue(StringUtils.isBlank(oldPassword),"请输入原始密码！");
        //原始密码是否正确
        AssertUtil.isTrue(!(user.getUserPwd().equals(Md5Util.encode(oldPassword))),"原始密码不正确！");
        //新密码非空
        AssertUtil.isTrue(StringUtils.isBlank(newPassword),"新密码不能为空");
        //新密码不能与原始密码相同
        AssertUtil.isTrue(newPassword.equals(oldPassword),"新密码不能与原始密码相同");
        //确认密码非空
        AssertUtil.isTrue(StringUtils.isBlank(confirmPwd),"确认密码不能为空");
        //确认密码和新密码一致
        AssertUtil.isTrue(!confirmPwd.equals(newPassword),"确认密码和新密码要一致！");
    }

    /*查询所有的销售人员*/
    public List<Map<String,Object>> querySales(){
        return userMapper.salectSales();
    }

    //用户模块的列表查询
    public Map<String,Object> findUserByParams(UserQuery userQuery){
        //实例化map
        Map<String,Object> map = new HashMap<>();
        //初始化分页单位
        PageHelper.startPage(userQuery.getPage(),userQuery.getLimit());
        //开始分页
        PageInfo<User> plist = new PageInfo<>(selectByParams(userQuery));
        //准备数据
        map.put("code",0);
        map.put("msg","success");
        map.put("count",plist.getTotal());
        map.put("data",plist.getList());
        //返回目标map
        return map;
    }

    //用户模块的添加


    @Transactional(propagation = Propagation.REQUIRED)
    public void addUser(User user ){
        //验证
        checkUser(user.getUserName(),user.getEmail(),user.getPhone());
        //用户名唯一
        User temp = userMapper.selectUserByName(user.getUserName());
        AssertUtil.isTrue(temp!=null,"用户名已经存在！");
        //设定默认值
        user.setIsValid(1);
        user.setCreateDate(new Date());
        user.setUpdateDate(new Date());
        //密码加密
        user.setUserPwd(Md5Util.encode("123456"));
        //验证是否成功
        AssertUtil.isTrue(insertSelective(user) < 1,"添加失败！！");
        //AssertUtil.isTrue(insertHasKey(user)<1,"添加失败！！");
        //该方法需要重写insertSelective，sql语句，并加上keyProperty="id" keyColumn="id" useGeneratedKeys="true"
        System.out.println(user.getId()+"<<<"+user.getRoleIds());
        //*****
        relaionUserRole(user.getId(),user.getRoleIds());
    }

    /**
     *
     * @param userId 用户id（唯一）
     * @param roleIds 角色id（量大）
     *
     *                原来有没有角色
     *                没有角色就添加角色
     *                有角色就删除所有角色再添加角色
     */
    private void relaionUserRole(Integer userId, String roleIds) {
        //准备集合存储对象
        List<UserRole> urlist = new ArrayList<>();
        //userId,roleId;
        AssertUtil.isTrue(StringUtils.isBlank(roleIds),"请选择角色信息");
        //统计当前角色。
        int count = userRoleMapper.countUserRoleNum(userId);
        //删除当前用户的角色
        if(count > 0){
            AssertUtil.isTrue(userRoleMapper.deleteUserRoleByUserId(userId) != count,"用户角色删除失败！");
        }
        //删除原来角色
        String[] RoleStrId = roleIds.split(",");
        //遍历
        for(String rid:RoleStrId){
            //准备对象
            UserRole userRole = new UserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(Integer.parseInt(rid));
            userRole.setCreateDate(new Date());
            userRole.setUpdateDate(new Date());
            //存放到集合
            urlist.add(userRole);
        }
        //批量添加
        AssertUtil.isTrue(userRoleMapper.insertBatch(urlist) != urlist.size(),"用户角色分配失败！");
    }

    private void checkUser(String userName, String email, String phone) {
        //用户名非空
        AssertUtil.isTrue(StringUtils.isBlank(userName),"用户名不能为空");
        //邮箱非空
        AssertUtil.isTrue(StringUtils.isBlank(email),"邮箱不能为空");
        //手机号飞空
        AssertUtil.isTrue(StringUtils.isBlank(phone),"手机号不能为空");
        //手机号合法
        AssertUtil.isTrue(!PhoneUtil.isMobile(phone),"输入合法的手机号");
    }

    //用户模块的修改
    @Transactional(propagation = Propagation.REQUIRED)//事务管理
    public void changeUser(User user){
        //根据id获取用户信息
        User temp = userMapper.selectByPrimaryKey(user.getId());
        //判断
        AssertUtil.isTrue(temp == null,"待修改的记录不存在");
        //验证参数
        checkUser(user.getUserName(),user.getEmail(),user.getPhone());
        //修改中用户名已存在问题
        User temp2 = userMapper.selectUserByName(user.getUserName());
        AssertUtil.isTrue(temp2 != null && !(temp2.getId().equals(user.getId())),"用户名已存在1");
        //设定默认值
        user.setUpdateDate(new Date());
        //判断是否修改成功
        AssertUtil.isTrue(updateByPrimaryKeySelective(user) < 1,"修改失败！");
        //
        relaionUserRole(user.getId(),user.getRoleIds());



    }

    @Transactional(propagation = Propagation.REQUIRED)//事务管理
    public void removeUserIds(Integer[] ids){
        //验证
        AssertUtil.isTrue(ids == null || ids.length == 0,"请选择要删除的数据");
        //遍历对象
        for (Integer userId: ids) {
            //
            int count = userRoleMapper.countUserRoleNum(userId);
            //删除当前用户角色
            if(count > 0){
                AssertUtil.isTrue(userRoleMapper.deleteUserRoleByUserId(userId) != count,"用户角色删除失败！");
            }
        }
        //判断删除是否成功
        AssertUtil.isTrue(userMapper.deleteBatch(ids) < 1,"删除失败！");
    }
}
