package com.intgroup.htmlcheck.service.security;

import com.intgroup.htmlcheck.domain.security.Role;
import com.intgroup.htmlcheck.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleService {
    @Autowired
    private RoleRepository roleRepository;

    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    public boolean roleExists(String roleName) {
        return roleRepository.findByRole(roleName) != null;
    }

    public void saveRole(Role role) {
        roleRepository.save(role);
    }

    public void deleteRole(long roleId) {
        roleRepository.deleteById(roleId);
    }

    public Role getRoleByName(String role) {
        return roleRepository.findByRole(role);
    }
}
