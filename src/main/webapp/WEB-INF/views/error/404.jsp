<%@ page contentType="text/html;charset=UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Page does not exist</title>
<link href="${ctx}/assets/admin/css/pages/error.css" rel="stylesheet" type="text/css" />
</head>
<body>
	<div class="row">
		<div class="col-md-12 page-404">
			<div class="number">404</div>
			<div class="details">
				<h3>Request address or page not found</h3>
				<p></p>
			</div>
		</div>
	</div>
</body>
</html>