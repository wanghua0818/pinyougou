package com.pinyougou.seckill.service.impl;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.List;


public class UserDetailServiceImpl implements UserDetailsService {
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        //用户认证的工作已交由cas负责 此处只负责查询权限
        List<GrantedAuthority> ga = new ArrayList<>();
        ga.add(new SimpleGrantedAuthority("ROLE_USER"));
        return new User(username, "", ga);
    }
}
