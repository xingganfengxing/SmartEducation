package com.hhit.action;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import com.hhit.base.BaseAction;
import com.hhit.entity.Course;
import com.hhit.entity.Department;
import com.hhit.entity.Role;
import com.hhit.entity.Teacher;
import com.hhit.entity.UserDetails;
import com.hhit.util.DepartmentUtils;
import com.hhit.util.QueryHelper;
import com.opensymphony.xwork2.ActionContext;

@SuppressWarnings("serial")
@Controller
@Scope("prototype")
public class CourseAction extends BaseAction<Course>{

	private Integer[] departmentIds;
	
	private String inputTerm = "";// 输入的词条
	
	private Integer[] courseIds;
	
	/** 列表 */
	public String list() throws Exception{

		new QueryHelper(Course.class, "c")//
		.addCondition((inputTerm.trim().length()>0), "c.courseName LIKE ?", "%"+inputTerm+"%")
		.preparePageBean(courseService, pageNum, pageSize);
		return "list";
	}
	/** 删除 */
	public String delete() throws Exception{
		courseService.delete(model.getId());
		return "toList";
	}
	/** 跳转课程添加界面 */
	public String addUI() throws Exception{
		//准备数据--部门
		List<Department> topList=departmentService.findTopList();
		ActionContext.getContext().put("departmentList", DepartmentUtils.getAllDepartments(topList));
		return "saveUI";
	}
	/** 添加 */
	public String add() throws Exception{
		//设置相关属性
		model.setAddTime(new Timestamp(new Date().getTime()));
		
		List<Department> deptList=departmentService.findByIds(departmentIds);
		model.setDepartments(new HashSet<>(deptList));
		//保存
		courseService.save(model);
		return "toList";
	}
	/** 跳转课程修改界面 */
	public String editUI() throws Exception{
		//准备数据--部门
		List<Department> topList=departmentService.findTopList();
		ActionContext.getContext().put("departmentList", DepartmentUtils.getAllDepartments(topList));
		//准备数据--课程
		Course courseFind=courseService.findById(model.getId());
		ActionContext.getContext().getValueStack().push(courseFind);
		//回显数据--部门
		if (courseFind.getDepartments() != null) {
			departmentIds = new Integer[courseFind.getDepartments().size()];
			int index = 0;
			for (Department dept : courseFind.getDepartments()) {
				departmentIds[index++] = dept.getId();
			}
		}
		return "saveUI";
	}
	/** 修改 */
	public String edit() throws Exception{
		//查询出原对象
		Course courseFind=courseService.findById(model.getId());
		//设置相关属性
		courseFind.setCourseName(model.getCourseName());
		courseFind.setDescription(model.getDescription());
		List<Department> deptList=departmentService.findByIds(departmentIds);
		courseFind.setDepartments(new HashSet<>(deptList));
		//更新数据库
		courseService.update(courseFind);
		return "toList";
	}
	/** 老师添加自己的课程界面 */
	public String addMyCourseUI() throws Exception{
		//准备数据--部门
		List<Department> topList=departmentService.findTopList();
		ActionContext.getContext().put("departmentList", DepartmentUtils.getAllDepartments(topList));
		//回显--老师所属部门
		Department deptFind;
		if((deptFind=getCurrentUser().getTeacher().getDepartment())!=null){
			departmentIds=new Integer[1];
			departmentIds[0]=deptFind.getId();
			//准备数据 -- 对应部门的所有课程
//			if(deptFind.getDeptLevel()>2){
				List<Course> courseList=new ArrayList<>(deptFind.getCourses()) ;
				ActionContext.getContext().put("courseList", courseList);
//			}
//			else{
//				List<Course> courseList=courseService.findByDepartment(deptFind);
//				ActionContext.getContext().put("courseList", courseList);
//			}
		}
		return "addMyCourseUI";
	}
	/** 老师添加自己的课程 */
	public String addNewMyCourse() throws Exception{
		//-->保存到课程表
		//设置相关属性
		model.setAddTime(new Timestamp(new Date().getTime()));
		
		List<Department> deptList=departmentService.findByIds(departmentIds);
		model.setDepartments(new HashSet<>(deptList));
		courseService.save(model);
		//保存
		//--》保存到老师任课表
		Teacher teaFind=getCurrentUser().getTeacher();
//		if(teaFind.getCourses()==null){
//			List<Course> courseList=new ArrayList<Course>();
//			courseList.add(model);
//			teaFind.setCourses(new HashSet<Course>(courseList));
//			teacherService.update(teaFind);
//		}
//		else{
			List<Course> courseList=new ArrayList<Course>(teaFind.getCourses());
			courseList.add(model);
			teaFind.setCourses(new HashSet<Course>(courseList));
			teacherService.update(teaFind);
//		}
		
		
		return "toTeachProcessList";
	}
	/**从现有课程中选择到老师的课程*/
	public String addMyCourse() throws Exception{
		//查找选择的课程
		List<Course> courseList=courseService.findByIds(courseIds);
		//找到老师
		Teacher teaFind=getCurrentUser().getTeacher();
		teaFind.setCourses(new HashSet<>(courseList));
		//更新
		teacherService.update(teaFind);
		return "toTeachProcessList";
	}
	
	public Integer[] getDepartmentIds() {
		return departmentIds;
	}
	public void setDepartmentIds(Integer[] departmentIds) {
		this.departmentIds = departmentIds;
	}
	public String getInputTerm() {
		return inputTerm;
	}
	public void setInputTerm(String inputTerm) {
		this.inputTerm = inputTerm;
	}
	public Integer[] getCourseIds() {
		return courseIds;
	}
	public void setCourseIds(Integer[] courseIds) {
		this.courseIds = courseIds;
	}
	
}
