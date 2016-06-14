<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Dashboard</title>
</head>
<body>
	<div class="note note-info">
		<h4 class="block">Prompt Description</h4>
		<p>
The following Dashboard content is mainly defined by the demonstration effect , according to the actual needs of custom design and development of project business requirements .</p>
	</div>
	<div class="row">
		<div class="col-md-3">
			<div class="dashboard-stat yellow">
				<div class="visual">
					<i class="fa fa-bullhorn"></i>
				</div>
				<div class="details">
					<div class="number number-notify-message-count">-</div>
					<div class="desc">Announcement unread messages</div>
				</div>
				<a href="javascript:;" rel="address:/admin/profile/notify-message|Announcement Message List" class="more">
					View more <i class="m-icon-swapright m-icon-white"></i>
				</a>
			</div>
		</div>
		<div class="col-md-3">
			<div class="dashboard-stat blue">
				<div class="visual">
					<i class="fa fa-envelope"></i>
				</div>
				<div class="details">
					<div class="number number-user-message-count">-</div>
					<div class="desc">Unread personal message</div>
				</div>
				<a href="javascript:;" rel="address:/admin/profile/user-message|Personal message list" class="more">
					View more <i class="m-icon-swapright m-icon-white"></i>
				</a>
			</div>
		</div>
		<div class="col-lg-3 col-md-3 col-sm-6 col-xs-12">
			<div class="dashboard-stat green">
				<div class="visual">
					<i class="fa fa-shopping-cart"></i>
				</div>
				<div class="details">
					<div class="number">549</div>
					<div class="desc">New Orders</div>
				</div>
				<a href="#" class="more">
					View more <i class="m-icon-swapright m-icon-white"></i>
				</a>
			</div>
		</div>
		<div class="col-lg-3 col-md-3 col-sm-6 col-xs-12">
			<div class="dashboard-stat purple">
				<div class="visual">
					<i class="fa fa-globe"></i>
				</div>
				<div class="details">
					<div class="number">+89%</div>
					<div class="desc">Brand Popularity</div>
				</div>
				<a href="#" class="more">
					View more <i class="m-icon-swapright m-icon-white"></i>
				</a>
			</div>
		</div>
	</div>
	<div class="well well-large">
		<h3>Project Description</h3>
		<p>Integration of front-end portal sites on the Internet for Web Applications build mainstream fashion latest open source technology , infrastructure development framework HTML5 mobile site and back-end management system integration , providing a major source J2EE related technology architecture to integrate enterprise applications , and some basic common features and components best practices and reference design prototype implementation.</p>
	</div>
	<c:if test="${cfg.dev_mode}">
		<div class="alert alert-block alert-info fade in">
			<h4 class="alert-heading">
				Tip Description : <small> the contents of the current interface only in development mode (dev_mode = true) to appear ! </ Small>
			</h4>
			<div class="row">
				<div class="col-md-12">
					<div class="list-group">
						<a class="list-group-item" href="${ctx}/docs/markdown/README" target="_blank">
						<H4 class = "list-group-item-heading"> Development Framework Introduction and Development Guide </ h4>
						<P class = "list-group-item-text"> development framework for the whole overall design description and development guidelines . </ P>
						</a>
						<a class="list-group-item" href="javascript:;" rel="address:/docs/ui-feature/items|UI组件用法示例">
						<H4 class = "list-group-item-heading"> UI Component Usage Example </ h4>
						<P class = "list-group-item-text"> typical UI components to provide a basic reference schematic usage , combined with the understanding of the functions corresponding to the document JSDoc attributes UI component usage and support . </ P>
						</a>
						<a class="list-group-item" href="${ctx}/docs/jsdoc/global.html" target="_blank">
						<H4 class = "list-group-item-heading"> Javascript comment JSDoc document </ h4>
						<P class = "list-group-item-text"> Frame is mainly based on Javascript code file comments, use reference document with the jsdoc3-maven-plugin generated UI components. </ P>
						</a>
					</div>
				</div>
			</div>
		</div>
	</c:if>
	<script type="text/javascript">
        $(function() {
            AdminGlobal.updateMessageCount();
        });
    </script>
</body>
</html>
