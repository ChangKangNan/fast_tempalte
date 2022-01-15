select name,dept_name from test_user
left join  test_dep on test_user.department_id=test_dep.dept_id
where test_dep.dept_name = #{depName}