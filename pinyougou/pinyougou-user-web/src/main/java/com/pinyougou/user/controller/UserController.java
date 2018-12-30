package com.pinyougou.user.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.common.util.PhoneFormatCheckUtils;
import com.pinyougou.pojo.TbUser;
import com.pinyougou.user.service.UserService;
import com.pinyougou.vo.PageResult;
import com.pinyougou.vo.Result;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.regex.PatternSyntaxException;

@RequestMapping("/user")
@RestController
public class UserController {

    @Reference
    private UserService userService;

    @RequestMapping("/findAll")
    public List<TbUser> findAll() {
        return userService.findAll();
    }

    @GetMapping("/findPage")
    public PageResult findPage(@RequestParam(value = "page", defaultValue = "1") Integer page,
                               @RequestParam(value = "rows", defaultValue = "10") Integer rows) {
        return userService.findPage(page, rows);
    }

    //return $http.post("user/add.do?smsCode=" + smsCode, entity);
    @PostMapping("/add")
    public Result add(@RequestBody TbUser user,String smsCode) {
        try {
            if (userService.checkChode(user.getPhone(),smsCode)){
                user.setCreated(new Date());
                user.setUpdated(user.getCreated());
                user.setPassword(DigestUtils.md5Hex(user.getPassword()));
                userService.add(user);
                return Result.ok("注册成功");
            }else {
                return Result.fail("验证码不正确");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.fail("注册失败");
    }

    //发送短信验证码 $http.get("user/sendSmsCode.do?phone=" +phone+"&r=" + Math.random());
    @GetMapping("/sendSmsCode")
    public Result sendSmsCode(String phone) {
        try {
            if (PhoneFormatCheckUtils.isPhoneLegal(phone)) {
                userService.sendSmsCode(phone);
                return Result.ok("发送验证码成功");
            } else {
                return Result.fail("手机号码格式有问题");
            }
        } catch (PatternSyntaxException e) {
            e.printStackTrace();
        }
        return Result.fail("发送验证码失败");
    }

    @GetMapping("/findOne")
    public TbUser findOne(Long id) {
        return userService.findOne(id);
    }

    @PostMapping("/update")
    public Result update(@RequestBody TbUser user) {
        try {
            userService.update(user);
            return Result.ok("修改成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.fail("修改失败");
    }

    @GetMapping("/delete")
    public Result delete(Long[] ids) {
        try {
            userService.deleteByIds(ids);
            return Result.ok("删除成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.fail("删除失败");
    }

    /**
     * 分页查询列表
     *
     * @param user 查询条件
     * @param page 页号
     * @param rows 每页大小
     * @return
     */
    @PostMapping("/search")
    public PageResult search(@RequestBody TbUser user, @RequestParam(value = "page", defaultValue = "1") Integer page,
                             @RequestParam(value = "rows", defaultValue = "10") Integer rows) {
        return userService.search(page, rows, user);
    }

}
