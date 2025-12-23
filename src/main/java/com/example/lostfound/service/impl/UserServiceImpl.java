package com.example.lostfound.service.impl;

import com.example.lostfound.mapper.UserMapper;
import com.example.lostfound.pojo.User;
import com.example.lostfound.pojo.dto.UserLoginDTO;
import com.example.lostfound.pojo.dto.UserRegisterDTO;
import com.example.lostfound.pojo.vo.PageResult;
import com.example.lostfound.pojo.vo.Result;
import com.example.lostfound.service.UserService;
import com.example.lostfound.util.JwtUtil;
import com.example.lostfound.util.OssUtil;
import com.example.lostfound.util.RedisUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户服务实现类
 */
@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RedisUtil redisUtil;

    @Value("${app.jwt.expiration}")
    private Long jwtExpiration;

    @Autowired
    private OssUtil ossUtil;

    @Override
    public Result<String> register(UserRegisterDTO registerDTO) {
        // 获取当前会话
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        HttpSession session = attributes.getRequest().getSession();
        
        // 获取session中的验证码
        String sessionCaptcha = (String) session.getAttribute("captcha");
        
        // 移除session中的验证码，确保验证码只能使用一次
        session.removeAttribute("captcha");
        
        // 校验验证码
        if (sessionCaptcha == null) {
            return Result.error("验证码已过期，请重新获取");
        }
        
        if (!sessionCaptcha.equalsIgnoreCase(registerDTO.getCaptcha())) {
            return Result.error("验证码错误");
        }
        
        // 检查用户名是否已存在
        User existUser = userMapper.selectByUsername(registerDTO.getUsername());
        if (existUser != null) {
            return Result.error("用户名已存在");
        }

        // 创建用户
        User user = new User();
        BeanUtils.copyProperties(registerDTO, user);

        // 密码加密
        user.setPassword(DigestUtils.md5DigestAsHex(registerDTO.getPassword().getBytes()));

        // 设置状态和时间
        user.setStatus(0); // 0正常，1禁用
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());

        // 插入用户
        userMapper.insert(user);

        return Result.success("注册成功");
    }

    @Override
    public Result<String> login(UserLoginDTO loginDTO) {
        // 获取当前会话
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        HttpSession session = attributes.getRequest().getSession();
        
        // 获取session中的验证码
        String sessionCaptcha = (String) session.getAttribute("captcha");
        
        // 移除session中的验证码，确保验证码只能使用一次
        session.removeAttribute("captcha");
        
        // 校验验证码
        if (sessionCaptcha == null) {
            return Result.error("验证码已过期，请重新获取");
        }
        
        if (!sessionCaptcha.equalsIgnoreCase(loginDTO.getCaptcha())) {
            return Result.error("验证码错误");
        }
        
        // 查询用户
        User user = userMapper.selectByUsername(loginDTO.getUsername());
        if (user == null) {
            return Result.error("用户名或密码错误");
        }

        // 检查密码
        String encryptPassword = DigestUtils.md5DigestAsHex(loginDTO.getPassword().getBytes());
        if (!user.getPassword().equals(encryptPassword)) {
            return Result.error("用户名或密码错误");
        }

        // 检查状态
        if (user.getStatus() == 1) {
            return Result.error("账号已被禁用");
        }

        // 生成token
        String token = jwtUtil.generateToken(String.valueOf(user.getId()), "user");

        // 存储token到Redis（24小时）
        String tokenKey = "token:" + token;
        // ✅ jwtExpiration是毫秒，需要转换为秒
        long expirationSeconds = jwtExpiration / 1000;
        redisUtil.set(tokenKey, user.getId() + ":" + "user", expirationSeconds);
        
        // ✅ 建立userId到token的反向索引，方便禁用时清除
        String userTokenSetKey = "user:tokens:" + user.getId();
        redisUtil.addToSet(userTokenSetKey, token);
        redisUtil.expire(userTokenSetKey, expirationSeconds);
        
        log.info("用户登录，token已存入Redis: key={}, value={}, expiration={}秒", tokenKey, user.getId() + ":" + "user", expirationSeconds);

        return Result.success(token);
    }

    @Override
    public Result<User> getUserInfo(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }

        // 清除敏感信息
        user.setPassword(null);

        return Result.success(user);
    }

    @Override
    public Result<String> updateUserInfo(User user) {
        // 查询用户
        User existUser = userMapper.selectById(user.getId());
        if (existUser == null) {
            return Result.error("用户不存在");
        }

        // 设置更新时间
        user.setUpdateTime(LocalDateTime.now());

        // 更新用户
        userMapper.update(user);

        return Result.success("更新成功");
    }

    @Override
    public Result<String> updateAvatar(Long userId, MultipartFile file) {
        // 检查文件是否为空
        if (file == null || file.isEmpty()) {
            return Result.error("上传文件不能为空");
        }
        
        // 查询用户
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }

        try {
            // 上传头像
            String avatarUrl = ossUtil.uploadAvatar(file);

            // 更新用户头像
            user.setAvatarUrl(avatarUrl);
            user.setUpdateTime(LocalDateTime.now());
            userMapper.update(user);

            return Result.success(avatarUrl);
        } catch (Exception e) {
            log.error("上传头像失败", e);
            return Result.error("上传头像失败");
        }
    }

    @Override
    public String uploadAvatar(MultipartFile file) {
        // 检查文件是否为空
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("上传文件不能为空");
        }
        
        // 直接上传头像，不关联到具体用户（用于注册时）
        return ossUtil.uploadAvatar(file);
    }

    @Override
    public Result<String> updatePassword(Long userId, String oldPassword, String newPassword) {
        // 查询用户
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }

        // 检查旧密码
        String encryptOldPassword = DigestUtils.md5DigestAsHex(oldPassword.getBytes());
        if (!user.getPassword().equals(encryptOldPassword)) {
            return Result.error("旧密码错误");
        }

        // 更新密码
        user.setPassword(DigestUtils.md5DigestAsHex(newPassword.getBytes()));
        user.setUpdateTime(LocalDateTime.now());
        userMapper.update(user);

        return Result.success("修改成功");
    }

    @Override
    public Result<List<User>> getUserList() {
        List<User> userList = userMapper.selectList();

        // 清除敏感信息
        userList.forEach(user -> user.setPassword(null));

        return Result.success(userList);
    }

    @Override
    public Result<PageResult<User>> getUserListWithPaging(int pageNum, int pageSize, String username, String realName, String studentNo) {
        // 使用PageHelper进行分页查询
        PageHelper.startPage(pageNum, pageSize);
        List<User> userList = userMapper.selectListByCondition(username, realName, studentNo);

        // 清除敏感信息
        userList.forEach(user -> user.setPassword(null));

        // 获取分页信息
        PageInfo<User> pageInfo = new PageInfo<>(userList);

        PageResult<User> pageResult = new PageResult<>();
        pageResult.setTotal(pageInfo.getTotal());
        pageResult.setList(pageInfo.getList());
        pageResult.setPageNum(pageInfo.getPageNum());
        pageResult.setPageSize(pageInfo.getPageSize());

        return Result.success(pageResult);
    }

    @Override
    public Result<String> updateStatus(Long userId, Integer status) {
        // 查询用户
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }

        // 更新状态
        userMapper.updateStatus(userId, status);
        
        // ✅ 如果是禁用用户（status=1），删除该用户的所有token
        if (status == 1) {
            clearUserTokens(userId);
            log.info("用户被禁用，已清除所有token: userId={}", userId);
        }

        return Result.success("操作成功");
    }

    @Override
    public Result<String> logout(String token) {
        // 从Redis中删除token
        String tokenKey = "token:" + token;
        redisUtil.delete(tokenKey);
        
        // ✅ 从用户token集合中移除
        try {
            String userIdStr = jwtUtil.getUserIdFromToken(token);
            if (userIdStr != null) {
                String userTokenSetKey = "user:tokens:" + userIdStr;
                redisUtil.removeFromSet(userTokenSetKey, token);
            }
        } catch (Exception e) {
            log.warn("从userTokenSet中移除token失败", e);
        }
        
        log.info("用户登出，token已从Redis中删除: key={}", tokenKey);
        return Result.success("登出成功");
    }
    
    /**
     * 清除用户的所有token（用于禁用用户时）
     * @param userId 用户ID
     */
    private void clearUserTokens(Long userId) {
        String userTokenSetKey = "user:tokens:" + userId;
        
        // 获取该用户的所有token
        java.util.Set<Object> tokens = redisUtil.getSetMembers(userTokenSetKey);
        
        if (tokens != null && !tokens.isEmpty()) {
            // 删除每个token
            for (Object tokenObj : tokens) {
                String token = tokenObj.toString();
                String tokenKey = "token:" + token;
                redisUtil.delete(tokenKey);
            }
            
            // 删除token集合本身
            redisUtil.delete(userTokenSetKey);
            
            log.info("已清除用户的{}个token: userId={}", tokens.size(), userId);
        }
    }
}









