package lab.s2jh.module.auth.web;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import lab.s2jh.core.annotation.MenuData;
import lab.s2jh.core.annotation.MetaData;
import lab.s2jh.core.pagination.GroupPropertyFilter;
import lab.s2jh.core.pagination.PropertyFilter;
import lab.s2jh.core.pagination.PropertyFilter.MatchType;
import lab.s2jh.core.security.AuthUserDetails;
import lab.s2jh.core.service.BaseService;
import lab.s2jh.core.service.Validation;
import lab.s2jh.core.util.EnumUtils;
import lab.s2jh.core.web.BaseController;
import lab.s2jh.core.web.view.OperationResult;
import lab.s2jh.module.auth.entity.Privilege;
import lab.s2jh.module.auth.entity.RoleR2Privilege;
import lab.s2jh.module.auth.entity.User;
import lab.s2jh.module.auth.entity.User.AuthTypeEnum;
import lab.s2jh.module.auth.entity.UserR2Role;
import lab.s2jh.module.auth.service.PrivilegeService;
import lab.s2jh.module.auth.service.RoleService;
import lab.s2jh.module.auth.service.UserService;
import lab.s2jh.module.sys.service.MenuService;
import lab.s2jh.module.sys.vo.NavMenuVO;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

@Controller
@RequestMapping(value = "/admin/auth/user")
public class UserController extends BaseController<User, Long> {

    @Autowired
    private UserService userService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private PrivilegeService privilegeService;

    @Autowired
    private MenuService menuService;

    @Override
    protected BaseService<User, Long> getEntityService() {
        return userService;
    }

    @RequiresUser
    @ModelAttribute
    public void prepareModel(HttpServletRequest request, Model model, @RequestParam(value = "id", required = false) Long id) {
        super.initPrepareModel(request, model, id);
    }

    @MenuData("Configuration Management: Rights Management : User accounts")
    @RequiresPermissions("Configuration Management: Rights Management : User accounts")
    @RequestMapping(value = "", method = RequestMethod.GET)
    public String index(Model model) {
        model.addAttribute("authTypeMap", EnumUtils.getEnumDataMap(AuthTypeEnum.class));
        return "admin/auth/user-index";
    }

    @RequiresPermissions("Configuration Management: Rights Management : User accounts")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ResponseBody
    public Page<User> findByPage(HttpServletRequest request) {
        return super.findByPage(User.class, request);
    }

    @RequestMapping(value = "/edit-tabs", method = RequestMethod.GET)
    public String editTabs(HttpServletRequest request) {
        return "admin/auth/user-inputTabs";
    }

    @RequiresPermissions("Configuration Management: Rights Management : User accounts")
    @RequestMapping(value = "/edit", method = RequestMethod.GET)
    public String editShow(Model model, @ModelAttribute("entity") User entity) {
        model.addAttribute("authTypeMap", EnumUtils.getEnumDataMap(AuthTypeEnum.class));
        model.addAttribute("roles", roleService.findAllCached());
        if (entity.isNew()) {
            entity.setMgmtGranted(true);
        }
        return "admin/auth/user-inputBasic";
    }

    @RequiresPermissions("Configuration Management: Rights Management : User accounts")
    @RequestMapping(value = "/edit", method = RequestMethod.POST)
    @ResponseBody
    public OperationResult editSave(@ModelAttribute("entity") User entity, Model model,
            @RequestParam(value = "rawPassword", required = false) String rawPassword) {
        Validation.notDemoMode();
        if (entity.isNew()) {
            Validation.isTrue(StringUtils.isNotBlank(rawPassword), "Create a user must set the initial password");
        }
        userService.saveCascadeR2Roles(entity, rawPassword);
        return OperationResult.buildSuccessResult("Data storage processing is completed", entity);
    }

    @RequiresPermissions("Configuration Management: Rights Management : User accounts")
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    @ResponseBody
    public OperationResult delete(@RequestParam("ids") Long... ids) {
        Validation.notDemoMode();
        return super.delete(ids);
    }

    @MetaData(value = "Summing the associated set of permissions")
    @RequiresPermissions("Configuration Management: Rights Management : User accounts")
    @RequestMapping(value = "/privileges", method = RequestMethod.GET)
    public String privileges(Model model, @ModelAttribute("entity") User entity) {
        Set<Long> r2PrivilegeIds = Sets.newHashSet();
        List<Privilege> privileges = privilegeService.findAllCached();
        List<UserR2Role> userR2Roles = entity.getUserR2Roles();
        boolean isAdmin = false;
        for (UserR2Role r2 : userR2Roles) {
            if (r2.getRole().getCode().equals(AuthUserDetails.ROLE_SUPER_USER)) {
                isAdmin = true;
                break;
            }
        }

        if (isAdmin) {
            for (Privilege privilege : privileges) {
                r2PrivilegeIds.add(privilege.getId());
            }
        } else {
            for (UserR2Role userR2Role : userR2Roles) {
                List<RoleR2Privilege> roleR2Privileges = userR2Role.getRole().getRoleR2Privileges();
                for (RoleR2Privilege roleR2Privilege : roleR2Privileges) {
                    r2PrivilegeIds.add(roleR2Privilege.getPrivilege().getId());
                }
            }
        }
        model.addAttribute("r2PrivilegeIds", StringUtils.join(r2PrivilegeIds, ","));
        return "admin/auth/user-privileges";
    }

    @MetaData(value = "Summing up the context menu set")
    @RequiresPermissions("Configuration Management: Rights Management : User accounts")
    @RequestMapping(value = "/menus", method = RequestMethod.GET)
    public String menusShow(Model model) {
        return "admin/auth/user-menus";
    }

    @RequiresPermissions("Configuration Management: Rights Management : User accounts")
    @RequestMapping(value = "/menus/data", method = RequestMethod.GET)
    @ResponseBody
    public Object menusData(Model model, @ModelAttribute("entity") User entity) {
        List<Map<String, Object>> items = Lists.newArrayList();
        List<NavMenuVO> navMenuVOs = menuService.processUserMenu(entity);
        for (NavMenuVO navMenuVO : navMenuVOs) {
            //ZTree assembly structure data
            Map<String, Object> item = Maps.newHashMap();
            item.put("id", navMenuVO.getId());
            item.put("pId", navMenuVO.getParentId());
            item.put("name", navMenuVO.getName());
            item.put("open", true);
            item.put("isParent", StringUtils.isBlank(navMenuVO.getUrl()));
            items.add(item);
        }
        return items;
    }

    @RequestMapping(value = "/tags", method = RequestMethod.GET)
    @ResponseBody
    public List<Map<String, Object>> tagsData(Model model, @RequestParam("q") String q) {
        GroupPropertyFilter groupFilter = GroupPropertyFilter.buildDefaultOrGroupFilter();
        groupFilter.append(new PropertyFilter(MatchType.CN, "authUid", q));
        groupFilter.append(new PropertyFilter(MatchType.CN, "nickName", q));
        groupFilter.append(new PropertyFilter(MatchType.CN, "email", q));
        groupFilter.append(new PropertyFilter(MatchType.CN, "mobile", q));
        List<User> users = userService.findByFilters(groupFilter);
        List<Map<String, Object>> items = Lists.newArrayList();
        for (User user : users) {
            Map<String, Object> item = Maps.newHashMap();
            item.put("id", user.getAlias());
            item.put("text", user.getAlias());
            items.add(item);
        }
        return items;
    }
}
