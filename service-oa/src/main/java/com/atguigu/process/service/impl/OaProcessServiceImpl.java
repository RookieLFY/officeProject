package com.atguigu.process.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.atguigu.auth.service.SysUserService;
import com.atguigu.model.process.Process;
import com.atguigu.model.process.ProcessRecord;
import com.atguigu.model.process.ProcessTemplate;
import com.atguigu.model.system.SysUser;
import com.atguigu.process.mapper.OaProcessMapper;
import com.atguigu.process.service.OaProcessRecordService;
import com.atguigu.process.service.OaProcessService;
import com.atguigu.process.service.OaProcessTemplateService;
import com.atguigu.security.custom.LoginUserInfoHelper;
import com.atguigu.vo.process.ApprovalVo;
import com.atguigu.vo.process.ProcessFormVo;
import com.atguigu.vo.process.ProcessQueryVo;
import com.atguigu.vo.process.ProcessVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;


import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.EndEvent;
import org.activiti.bpmn.model.FlowNode;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricTaskInstanceQuery;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipInputStream;

/**
 * <p>
 * 审批类型 服务实现类
 * </p>
 *
 * @author atguigu
 * @since 2023-03-10
 */
@Service
public class OaProcessServiceImpl extends ServiceImpl<OaProcessMapper,Process> implements OaProcessService {
    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private OaProcessTemplateService processTemplateService;
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private OaProcessRecordService processRecordService;
    @Autowired
    private HistoryService historyService;

    //审批管理中列表方法
    @Override
    public IPage<ProcessVo> selectPage(Page<ProcessVo> pageParam, ProcessQueryVo processQueryVo) {
        IPage<ProcessVo> pagemodel = baseMapper.selectPage(pageParam, processQueryVo);

        return pagemodel;
    }

    //部署流程定义
    @Override
    public void deployByZip(String deployPath) {
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(deployPath);
        ZipInputStream zipInputStream = new ZipInputStream(inputStream);
        //部署
        Deployment deploy = repositoryService.createDeployment().addZipInputStream(zipInputStream).deploy();
        System.out.println(deploy.getId());
        System.out.println(deploy.getName());

    }


    //启动流程实例
    @Override
    public void startUp(ProcessFormVo processFormVo) {
        //1：根据当前的用户id获取用户信息
        SysUser sysUser = sysUserService.getById(LoginUserInfoHelper.getUserId());

        //2：根据审批模板id把模板信息查询
        ProcessTemplate processTemplate = processTemplateService.getById(processFormVo.getProcessTemplateId());

        //3：保存提交的审批信息到业务表，oa_process
        Process process = new Process();
        //processFormVo复制到process中去
        BeanUtils.copyProperties(processFormVo,process);
        //其他值
        process.setStatus(1);//审批中
        String workNo = System.currentTimeMillis() + "";
        process.setProcessCode(workNo);
        process.setUserId(LoginUserInfoHelper.getUserId());
        process.setFormValues(processFormVo.getFormValues());
        process.setTitle(sysUser.getName() + "发起" + processTemplate.getName() + "申请");
        baseMapper.insert(process);

        //4：启动流程实例-RuntimeService
        //4.1：流程定义
        String processDefinitionKey = processTemplate.getProcessDefinitionKey();
        //4.2：业务key processId
        String businessKey = String.valueOf(process.getId());
        //4.3：流程参数form表单json数据，转换map集合
        String formValues = processFormVo.getFormValues();
        JSONObject jsonObject = JSON.parseObject(formValues);
        JSONObject formData = jsonObject.getJSONObject("formData");
        Map<String,Object> map = new HashMap<>();
        for(Map.Entry<String,Object> entry:formData.entrySet()){
            map.put(entry.getKey(),entry.getValue());
        }
        Map<String,Object> variables = new HashMap<>();
        variables.put("data",map);
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(processDefinitionKey, businessKey, variables);


        //5：查询下一个审批人
        List<Task> taskList = this.getCurrentTaskList(processInstance.getId());
        List<String> nameList = new ArrayList<>();
        for(Task task:taskList){
            String assigneeName = task.getAssignee();
            SysUser user = sysUserService.getUserByUserName(assigneeName);
            String name = user.getName();
            nameList.add(name);
            //6：推送消息

        }

        process.setProcessInstanceId(processInstance.getId());
        process.setDescription("等待"+nameList.toString()+"审批");
        //7：把当前业务和流程进行最终关联
        baseMapper.updateById(process);
        processRecordService.record(process.getId(),1,"发起申请");
    }


    //查询待处理任务的列表
    @Override
    public IPage<ProcessVo> findPending(Page<java.lang.Process> pageParam) {
        //1：封装查询条件，根据当前登录的用户名称
        TaskQuery query = taskService.createTaskQuery().taskAssignee(LoginUserInfoHelper.getUsername()).orderByTaskCreateTime().desc();

        //2：调用方法分页条件查询，返回list集合，代办任务集合
        //listPage方法有两个参数
        //第一个参数：开始位置  第二个参数：每页显示记录数
        int begin = (int)((pageParam.getCurrent()-1)*pageParam.getSize());
        int size = (int)pageParam.getSize();
        List<Task> taskList = query.listPage(begin, size);
        long totalCount = query.count();
        //3：封装返回list集合数据，到List<ProcessVo>里面
        List<ProcessVo> processVoList = new ArrayList<>();
        for(Task task : taskList){
            //从task获取流程实例
            String processInstanceId = task.getProcessInstanceId();

            //根据流程实例id获取实例对象
            ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();

            //从流程实例对象获取业务key
            String businessKey = processInstance.getBusinessKey();
            if(businessKey==null){
                continue;
            }
            //根据业务key获取Process对象
            long processId = Long.parseLong(businessKey);
            Process process = baseMapper.selectById(processId);
            //把process对象，复制ProcessVo对象
            ProcessVo processVo = new ProcessVo();
            BeanUtils.copyProperties(process,processVo);
            processVo.setTaskId(task.getId());
            //放到最终list集合
            processVoList.add(processVo);

        }
        //4：封装返IPage对象
        IPage<ProcessVo> page = new Page<ProcessVo>(pageParam.getCurrent(),pageParam.getSize(),totalCount);
        page.setRecords(processVoList);
        return page;
    }


    //查看审批详情信息
    @Override
    public Map<String, Object> show(Long id) {
        Process process = baseMapper.selectById(id);
        LambdaQueryWrapper<ProcessRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProcessRecord::getProcessId,id);
        List<ProcessRecord> processRecordList = processRecordService.list(wrapper);
        ProcessTemplate processTemplate = processTemplateService.getById(process.getProcessTemplateId());
        boolean isApprove = false;
        List<Task> taskList = this.getCurrentTaskList(process.getProcessInstanceId());
        String username = LoginUserInfoHelper.getUsername();
        for(Task task:taskList){
            if(task.getAssignee().equals(username)){
                isApprove=true;
            }
        }
        Map<String,Object> map = new HashMap<>();
        map.put("process", process);
        map.put("processRecordList", processRecordList);
        map.put("processTemplate", processTemplate);
        map.put("isApprove", isApprove);
        return map;
    }


    //审批
    @Override
    public void approve(ApprovalVo approvalVo) {
        //1:从approvalVo获取任务id，根据任务id获取流程变量
        String taskId = approvalVo.getTaskId();
        Map<String, Object> variables = taskService.getVariables(taskId);
        for(Map.Entry<String,Object> entry:variables.entrySet()){
            System.out.println(entry.getKey());
            System.out.println(entry.getValue());
        }
        //2：判断审批状态值
        if(approvalVo.getStatus() == 1){
            //2.1：状态值=1表示审批通过
            Map<String,Object> variable = new HashMap<>();
            taskService.complete(taskId,variable);
        }else{
            //2.2状态值=-1 驳回，流程直接结束
            this.endTask(taskId);
        }

        //3：记录审批相关过程的信息
        String descripetion = approvalVo.getStatus().intValue() == 1 ? "已通过" : "驳回";
        processRecordService.record(approvalVo.getProcessId(),
                                    approvalVo.getStatus(),
                                    descripetion);

        //4：查询下一个审批人
        Process process = baseMapper.selectById(approvalVo.getProcessId());
        List<Task> taskList = this.getCurrentTaskList(process.getProcessCode());
        if(!CollectionUtils.isEmpty(taskList)){
            List<String> assignList = new ArrayList<>();
            for (Task task:taskList){
                String assignee = task.getAssignee();
                SysUser sysUser = sysUserService.getUserByUserName(assignee);
                assignList.add(sysUser.getName());
            }
            process.setDescription("等待" + StringUtils.join(assignList.toArray(), ",") + "审批");
            process.setStatus(1);
        }else{
            if(approvalVo.getStatus().intValue() == 1){
                process.setDescription("审批完成（通过）");
                process.setStatus(2);
            }else{
                process.setDescription("审批完成（驳回）");
                process.setStatus(-1);
            }
        }
        baseMapper.updateById(process);
    }

    //已处理
    @Override
    public IPage<ProcessVo> findProcessed(Page<java.lang.Process> pageParam) {
        //封装查询条件
        HistoricTaskInstanceQuery query = historyService.createHistoricTaskInstanceQuery()
                .taskAssignee(LoginUserInfoHelper.getUsername())
                .finished().orderByTaskCreateTime().desc();
        //调用方法条件分页查询，返回list集合
        int begin = (int)((pageParam.getCurrent()-1)*pageParam.getSize());
        int size = (int)pageParam.getSize();
        List<HistoricTaskInstance> list = query.listPage(begin, size);
        long totalCount = query.count();

        //遍历返回list集合，封装List<ProcessVo>
        List<ProcessVo> processVoList = new ArrayList<>();
        for(HistoricTaskInstance item:list){
            String processInstanceId = item.getProcessInstanceId();
            LambdaQueryWrapper<Process> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Process::getProcessInstanceId,processInstanceId);
            Process process = baseMapper.selectOne(wrapper);
            ProcessVo processVo = new ProcessVo();
            BeanUtils.copyProperties(process,processVo);
            //放到list
            processVoList.add(processVo);
        }
        IPage<ProcessVo> pageModel = new Page<ProcessVo>(pageParam.getCurrent(),pageParam.getSize(),totalCount);
        pageModel.setRecords(processVoList);
        return pageModel;
    }

    //已发起
    @Override
    public IPage<ProcessVo> findStarted(Page<ProcessVo> pageParam) {
        ProcessQueryVo processQueryVo = new ProcessQueryVo();
        processQueryVo.setUserId(LoginUserInfoHelper.getUserId());
        IPage<ProcessVo> pageModel = baseMapper.selectPage(pageParam, processQueryVo);
        return pageModel;
    }

    //结束流程
    private void endTask(String taskId) {
        //  当前任务
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        BpmnModel bpmnModel = repositoryService.getBpmnModel(task.getProcessDefinitionId());
        List<EndEvent> endEventList = bpmnModel.getMainProcess().findFlowElementsOfType(EndEvent.class);
        // 并行任务可能为null
        if(CollectionUtils.isEmpty(endEventList)) {
            return;
        }
        FlowNode endFlowNode = (FlowNode) endEventList.get(0);
        FlowNode currentFlowNode = (FlowNode) bpmnModel.getMainProcess().getFlowElement(task.getTaskDefinitionKey());

        //  临时保存当前活动的原始方向
        List originalSequenceFlowList = new ArrayList<>();
        originalSequenceFlowList.addAll(currentFlowNode.getOutgoingFlows());
        //  清理活动方向
        currentFlowNode.getOutgoingFlows().clear();

        //  建立新方向
        SequenceFlow newSequenceFlow = new SequenceFlow();
        newSequenceFlow.setId("newSequenceFlowId");
        newSequenceFlow.setSourceFlowElement(currentFlowNode);
        newSequenceFlow.setTargetFlowElement(endFlowNode);
        List newSequenceFlowList = new ArrayList<>();
        newSequenceFlowList.add(newSequenceFlow);
        //  当前节点指向新的方向
        currentFlowNode.setOutgoingFlows(newSequenceFlowList);

        //  完成当前任务
        taskService.complete(task.getId());
    }


    //当前任务的列表
    private List<Task> getCurrentTaskList(String id) {
        List<Task> taskList = taskService.createTaskQuery().processInstanceId(id).list();
        return taskList;
    }
}
