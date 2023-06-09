package com.itheima.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.reggie.entity.Employee;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author qiao
 * @create 2023-03-11 9:59
 */
@Mapper
public interface EmployeeMapper extends BaseMapper<Employee> {
}
