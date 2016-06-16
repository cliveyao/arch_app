<%@ page contentType="text/html;charset=UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<body>
	<div class="row search-form-default">
		<div class="col-md-12">
			<form action="${ctx}/admin/profile/user-message-list?readed=${param.readed}" method="get"
				class="form-inline form-validation form-search-init control-label-sm"
				data-div-search=".profile-user-message-List">
				<div class="btn-group">
					<a class="btn btn-default ${empty param.readed?'active':''}" href='javascripts:;'
						rel='address:/admin/profile/user-message|Personal message list'>Complete</a> <a
						class="btn btn-default ${param.readed=='no'?'active':''}" href='javascripts:;'
						rel='address:/admin/profile/user-message?readed=no|Unread personal messages list '> Unread</a> <a
						class="btn btn-default ${param.readed=='yes'?'active':''}" href='javascripts:;'
						rel='address:/admin/profile/user-message?readed=yes|Read a list of personal messages '> Read</a>
				</div>
				<div class="form-group">
					<div class="controls controls-clearfix">
						<input type="text" name="search['CN_title']" class="form-control input-medium" placeholder="Message title ...">
					</div>
				</div>
				<div class="form-group">
					<div class="controls controls-clearfix">
						<input type="text" name="search['CN_message']" class="form-control input-medium" placeholder="Message content ...">
					</div>
				</div>
				<div class="form-group">
					<label class="control-label">Release date</label>
					<div class="controls">
						<input type="text" name="search['EQ_createdDate']" class="form-control input-small" data-picker="date">
					</div>
				</div>
				<div class="form-group search-group-btn">
					<button class="btn green" type="submmit">
						<i class="m-icon-swapright m-icon-white"></i>&nbsp; Inquiry
					</button>
					<button class="btn default" type="reset">
						<i class="fa fa-undo"></i>&nbsp; Refresh
					</button>
				</div>

			</form>
		</div>
	</div>

	<div class="profile-user-message-List"></div>
</body>
</html>