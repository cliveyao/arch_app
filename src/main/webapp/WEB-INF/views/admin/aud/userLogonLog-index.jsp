<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="tags" tagdir="/WEB-INF/tags"%>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
</head>
<body>
	<div class="row search-form-default">
		<div class="col-md-12">
			<form method="get" class="form-inline form-validation form-search form-search-init control-label-sm"
				data-grid-search="#grid-aud-user-logon-log-index">
				<div class="form-group">
					<div class="controls controls-clearfix">
						<input type="text" name="search['CN_authUid_OR_xforwardFor']" class="form-control input-xlarge"
							placeholder="Login account , xforwardFor...">
					</div>
				</div>
				<div class="form-group search-group-btn">
					<button class="btn green" type="submmit">
						<i class="m-icon-swapright m-icon-white"></i>&nbsp; Inquiry
					</button>
					<button class="btn default" type="reset">
						<i class="fa fa-undo"></i>&nbsp; Reset
					</button>
				</div>
			</form>
		</div>
	</div>
	<div class="row">
		<div class="col-md-12">
			<table id="grid-aud-user-logon-log-index"></table>
		</div>
	</div>
	<script type="text/javascript">
        $(function() {
            $("#grid-aud-user-logon-log-index").data("gridOptions", {
                url : WEB_ROOT + '/admin/aud/user-logon-log/list',
                colModel : [ {
                    label : 'Login account',
                    name : 'authUid',
                    width : 100,
                    align : 'center'
                }, {
                    label : 'Account number',
                    name : 'authGuid',
                    width : 100,
                    hidden : true,
                    align : 'left'
                }, {
                    label : 'Log in time',
                    name : 'logonTime',
                    formatter : 'timestamp'
                }, {
                    label : 'Logout Time',
                    name : 'logoutTime',
                    formatter : 'timestamp'
                }, {
                    label : 'Log Duration',
                    name : 'logonTimeLengthFriendly',
                    index : 'logonTimeLength',
                    width : 100,
                    fixed : true,
                    align : 'center'
                }, {
                    label : 'Logins',
                    name : 'logonTimes',
                    width : 60,
                    formatter : 'integer',
                    align : 'center'
                }, {
                    name : 'userAgent',
                    hidden : true,
                    align : 'left'
                }, {
                    name : 'xforwardFor',
                    width : 100,
                    align : 'center'
                }, {
                    name : 'localAddr',
                    hidden : true,
                    align : 'left'
                }, {
                    name : 'localName',
                    hidden : true,
                    align : 'left'
                }, {
                    name : 'localPort',
                    width : 60,
                    fixed : true,
                    hidden : true,
                    align : 'right'
                }, {
                    name : 'remoteAddr',
                    hidden : false,
                    align : 'center'
                }, {
                    name : 'remoteHost',
                    hidden : true,
                    align : 'left'
                }, {
                    name : 'remotePort',
                    width : 60,
                    fixed : true,
                    hidden : true,
                    align : 'right'
                }, {
                    name : 'serverIP',
                    hidden : true,
                    align : 'left'
                }, {
                    name : 'httpSessionId',
                    hidden : true,
                    align : 'left'
                } ],
                multiselect : false,
                sortorder : "desc",
                sortname : "logonTime",
                addable : false,
                fullediturl : WEB_ROOT + '/admin/aud/user-logon-log/edit-tabs'
            });
        });
    </script>
</body>
</html>
