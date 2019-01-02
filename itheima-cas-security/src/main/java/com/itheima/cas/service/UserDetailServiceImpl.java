package com.itheima.cas.service;

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
        //用户认证工作已交给cas,当前类只做查询用户权限
        List<GrantedAuthority> ga = new ArrayList<>();
        ga.add(new SimpleGrantedAuthority("ROLE_USER"));
        return new User(username, "", ga);
    }
}
