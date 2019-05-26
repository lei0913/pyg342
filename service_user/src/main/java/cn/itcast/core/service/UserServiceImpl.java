package cn.itcast.core.service;

import cn.itcast.core.dao.user.UserDao;
import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.pojo.user.User;
import cn.itcast.core.pojo.user.UserQuery;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.apache.activemq.command.ActiveMQQueue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.Session;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private JmsTemplate jmsTemplate;

    //点对点发送, 验证码到这个目标中, 队列名称叫做sms
    @Autowired
    private ActiveMQQueue smsDestination;

    @Autowired
    private UserDao userDao;

    //模板编号
    @Value("${template_code}")
    private String templateCode;

    //签名
    @Value("${sign_name}")
    private String singName;


    @Override
    public void sendCode(final String phone) {

        //1. 生成一个随机六位以内的数字作为短信验证码
        final long code = (long)(Math.random() * 1000000);
        //2. 将手机号作为key, 验证码作为value保存到redis, 生存时间为10分钟
        redisTemplate.boundValueOps(phone).set(code, 10, TimeUnit.MINUTES);
        //3. 将手机号, 验证码, 模板编号, 签名等内容封装成Map类型的消息, 发送给消息服务器
        //消息服务器在这里起到高并发的时候的缓冲功能.
        jmsTemplate.send(smsDestination, new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                //创建Map类型的消息对象
                MapMessage mapMessage = session.createMapMessage();
                //模板编号
                mapMessage.setString("templateCode", templateCode);
                //签名
                mapMessage.setString("singName", singName);
                //手机号
                mapMessage.setString("phone", phone);

                //封装短信内容, 短信内容必须是json格式, 这里放入map中使用工具转换成json
                Map<String, String> contentMap = new HashMap<>();
                contentMap.put("code", String.valueOf(code));
                //短信内容
                mapMessage.setString("content", JSON.toJSONString(contentMap));
                return mapMessage;
            }
        });



    }

    @Override
    public boolean checkCode(String phone, String smsCode) {
        //1. 校验手机号和验证码不为空
        if (phone == null || "".equals(phone) || smsCode ==null || "".equals(smsCode)) {
            return false;
        }
        //2. 根据手机号到redis中获取验证码
        Long redisSmsCode = (Long)redisTemplate.boundValueOps(phone).get();
        //3. 判断如果获取不到验证码直接返回false校验失败
        if (redisSmsCode == null || "".equals(redisSmsCode)) {
            return false;
        }
        //4. 判断页面传入的验证码是否等于我们redis自己保存的验证码
        if (smsCode.equals(String.valueOf(redisSmsCode))) {
            //5. 校验验证码正确, 返回正确信息并且将redis中对应的验证码删除
            redisTemplate.delete(phone);
            return true;
        }
        return false;
    }

    @Override
    public void add(User user) {
        user.setCreated(new Date());
        user.setUpdated(new Date());
        //新添加的用户都是正常用户
        user.setStatus("Y");
        userDao.insertSelective(user);
    }

    public static void main(String[] args) {
        long random = (long)(Math.random() * 1000000);
        System.out.println("=======" + random);
    }

    /**
     * 改
     * @param user
     * @param page
     * @param rows
     * @return
     */
    @Override
    public PageResult findPage(User user, Integer page, Integer rows) {
       /* *//**
         * 缓存模板中的数据到redis中, 供前台搜索使用
         *//*
        List<User> users = userDao.selectByExample(null);
        if (user != null) {
            for (User user1 : users) {
                //1.获取用户名字符串
                String user1NameStr = user1.getName();
                //2. 将用户名json字符串转换成集合
                List<Map> brandList = JSON.parseArray(user1NameStr, Map.class);
                //3. 将用户id作为小key, 名字作为value缓存入redis中
                redisTemplate.boundHashOps(Constants.REDIS_BRANDLIST).put(user1.getId(), brandList);
            }
        }*/
        //分页
        PageHelper.startPage(page, rows);
        //创建查询对象
        UserQuery userQuery = new UserQuery();
        UserQuery.Criteria criteria = userQuery.createCriteria();
        if (user != null) {
            if (user.getName() != null && !"".equals(user.getName())) {
                criteria.andNameLike("%" + user.getName() + "%");
            }
        }
        Page<User> userPage = (Page<User>) userDao.selectByExample(userQuery);

        return new PageResult(userPage.getTotal(), userPage.getResult());
    }

    /**
     * 继续改
     * @param ids
     * @param status
     */
    //开始审核  参数1:数组 商品表 的ID    参数2： 驳回  2
    @Override
    public void updateStatus(Long[] ids, String status) {
        for (Long id : ids) {
            User user = userDao.selectByPrimaryKey(id);
            user.setStatus(status);
            userDao.updateByPrimaryKeySelective(user);

        }
    }


}
