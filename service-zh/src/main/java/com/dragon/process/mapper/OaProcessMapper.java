package com.dragon.process.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dragon.model.process.Process;
import com.dragon.vo.process.ProcessQueryVo;
import com.dragon.vo.process.ProcessVo;
import org.apache.ibatis.annotations.Param;


/**
 * <p>
 * 审批类型 Mapper 接口
 * </p>
 *
 * @author fzt
 * @since 2024-03-28
 */
public interface OaProcessMapper extends BaseMapper<Process> {
    IPage<ProcessVo> selectPage(Page<ProcessVo> pageParam,@Param("vo") ProcessQueryVo processQueryVo);
}
