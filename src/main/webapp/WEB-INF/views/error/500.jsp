<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>System Abnormal</title>
<link href="${ctx}/assets/admin/css/pages/error.css" rel="stylesheet" type="text/css" />
</head>
<body>
	<div class="row">
		<div class="col-md-12 page-500">
			<div class=" number">500</div>
			<div class=" details">
				<h3><%=request.getAttribute("javax.servlet.error.message")%></h3>
				<p>
					Try refreshing the page or login again after the operation again . If the problem persists please the above error message back to the system administrator.<br> <br>
				</p>
			</div>
		</div>
	</div>
</body>
</html>